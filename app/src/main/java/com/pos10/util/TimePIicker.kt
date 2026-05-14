package com.fluper.curve_user_android.ui.m5

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.pos10.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

//old but working
fun showTimePickerDialog(
    context: Context,
    initialHour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
    initialMinute: Int = Calendar.getInstance().get(Calendar.MINUTE),
    onTimeSelected: (Long) -> Unit,
    onDismiss: () -> Unit, // ⬅️ new callback for cancel/dismiss
    themeResId: Int = R.style.CustomDatePicker
) {
    val timePickerDialog = TimePickerDialog(
        context,
        themeResId,
        { _, hourOfDay, minute ->
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hourOfDay)
                set(Calendar.MINUTE, minute)
            }
            onTimeSelected(cal.time.time)
        },
        initialHour,
        initialMinute,
        false
    )

    // ⬇️ Handle cancel and dismiss
    timePickerDialog.setOnCancelListener {
        onDismiss()
    }
    timePickerDialog.setOnDismissListener {
        onDismiss()
    }

    timePickerDialog.show()
}


//second with current date check
fun showTimePickerDialog(
    context: Context,
    selectedDate: String, // Format: "dd/MM/yyyy"
    dueDateString: String, // Format: "dd-MM-yyyy HH:mm:ss"
    onTimeSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
    preSelectedTime: String? = null, // Format: "hh:mm a" (e.g., "02:22 PM")
    themeResId: Int = R.style.CustomDatePicker) {
    val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
    val dueDate = try {
        formatter.parse(dueDateString)
    } catch (e: ParseException) {
        e.printStackTrace()
        null
    }

    if (dueDate == null) {
        onDismiss()
        return
    }

    val initialCalendar = Calendar.getInstance()

    // ⏰ Set time from preSelectedTime
    if (!preSelectedTime.isNullOrBlank()) {
        try {
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val parsedTime = timeFormat.parse(preSelectedTime)
            parsedTime?.let {
                val cal = Calendar.getInstance()
                cal.time = it
                initialCalendar.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY))
                initialCalendar.set(Calendar.MINUTE, cal.get(Calendar.MINUTE))
            }
        } catch (e: ParseException) {
            e.printStackTrace() // fallback to current time
        }
    }

    val initialHour = initialCalendar.get(Calendar.HOUR_OF_DAY)
    val initialMinute = initialCalendar.get(Calendar.MINUTE)

    val timePickerDialog = TimePickerDialog(
        context,
        themeResId,
        { _, hourOfDay, minute ->
            try {
                val dateParts = selectedDate.split("/")
                val selectedDateTime = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, dateParts[0].toInt())
                    set(Calendar.MONTH, dateParts[1].toInt() - 1)
                    set(Calendar.YEAR, dateParts[2].toInt())
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                }

                val selectedMillis = selectedDateTime.timeInMillis
                val nowMillis = System.currentTimeMillis()

                val today = Calendar.getInstance()
                val isToday =
                    today.get(Calendar.YEAR) == selectedDateTime.get(Calendar.YEAR) &&
                            today.get(Calendar.DAY_OF_YEAR) == selectedDateTime.get(Calendar.DAY_OF_YEAR)

                if ((isToday && selectedMillis < nowMillis) || selectedMillis > dueDate.time) {
                    Toast.makeText(context, "Due date has already passed.", Toast.LENGTH_SHORT).show()
                    onDismiss()
                    return@TimePickerDialog
                }

                onTimeSelected(selectedMillis)
            } catch (e: Exception) {
                e.printStackTrace()
                onDismiss()
            }
        },
        initialHour,
        initialMinute,
        false // Use 12-hour format
    )

    timePickerDialog.setOnCancelListener { onDismiss() }
    timePickerDialog.setOnDismissListener { onDismiss() }
    timePickerDialog.show()
}

fun showDatePickerDialogWithoutDueDate(
    context: Context,
    initialYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    initialMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    initialDay: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit, // ⬅️ add this to handle cancel/dismiss
    themeResId: Int = R.style.CustomDatePicker
) {
    val datePickerDialog = DatePickerDialog(
        context,
        themeResId,
        { _, year, monthOfYear, dayOfMonth ->
            val selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year)
            onDateSelected(selectedDate)
        },
        initialYear,
        initialMonth,
        initialDay)

    // Prevent selecting past dates
    val calendar = Calendar.getInstance()
    datePickerDialog.datePicker.maxDate = calendar.timeInMillis

    // ⬇️ Handle cancel and dismiss cases
    datePickerDialog.setOnCancelListener {
        onDismiss()
    }
    datePickerDialog.setOnDismissListener {
        onDismiss()
    }

    datePickerDialog.show()
}

fun showDatePickerDialog(
    context: Context,
    dueDateString: String, // Format: "dd-MM-yyyy HH:mm:ss"
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    preSelectedDateString: String? = null, // Format: "dd/MM/yyyy"
    themeResId: Int = R.style.CustomDatePicker
) {
    try {
        val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val dueDate = formatter.parse(dueDateString)
        val now = Calendar.getInstance()

        if (dueDate == null || dueDate.time < now.timeInMillis) {
            Toast.makeText(context, "Due date has already passed.", Toast.LENGTH_SHORT).show()
            onDismiss()
            return
        }

        // Initialize calendar with either current date or previously selected date
        val initialCalendar = Calendar.getInstance()

        if (!preSelectedDateString.isNullOrBlank()) {
            try {
                val preSelectedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(preSelectedDateString)
                if (preSelectedDate != null) {
                    initialCalendar.time = preSelectedDate
                }
            } catch (e: Exception) {
                // fallback to today
            }
        }

        val initialYear = initialCalendar.get(Calendar.YEAR)
        val initialMonth = initialCalendar.get(Calendar.MONTH)
        val initialDay = initialCalendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            themeResId,
            { _, year, monthOfYear, dayOfMonth ->
                val selectedDateStr = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year)
                onDateSelected(selectedDateStr)
            },
            initialYear,
            initialMonth,
            initialDay
        )

        datePickerDialog.datePicker.minDate = now.timeInMillis
        datePickerDialog.datePicker.maxDate = dueDate.time

        datePickerDialog.setOnCancelListener { onDismiss() }
        datePickerDialog.setOnDismissListener { onDismiss() }

        datePickerDialog.show()

    } catch (e: Exception) {
        onDismiss() // silent fail
    }
}


