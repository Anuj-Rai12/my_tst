package com.pos10.view.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pos10.R
import com.pos10.domain.AuthViewModel
import com.pos10.helper.CommonUtils.convertDateTimeFormat
import com.pos10.helper.CommonUtils.returnAccessToken
import com.pos10.helper.CommonUtils.showToastC
import com.pos10.helper.SharedPreference
import com.pos10.model.local.SaveApppointmentRequest
import com.pos10.view.SelectedRequestHolder

@Composable
fun ScheduleAppointment(viewModel: AuthViewModel, onDismiss: () -> Unit) {
    Log.d("TAG", "ScheduleAppointment:Date---${viewModel.selectDatePicker.value} ")
    val context = LocalContext.current
    Dialog(onDismissRequest = {
        onDismiss()
        viewModel.showDatePicker = false
        viewModel.showTimePicker = false
    }, properties = DialogProperties(dismissOnClickOutside = true)) {
        Surface(shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)) {

                Text(text = "Schedule Appointment",
                    color = Color(0xFF333333),
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)))
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Select Date",
                        color = Color(0xFF333333),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Start,
                        fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)))
                    Spacer(modifier = Modifier.width(24.dp))
                    Text(text = "Select Time",
                        color = Color(0xFF333333),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 28.dp),
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Date & Time Pickers Row
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                    // Date Picker Box
                    Box(modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clickable {
                            viewModel.showTimePicker = false
                            viewModel.showDatePicker = true
                        }
                        .background(Color(0xFFF6F6F6), RoundedCornerShape(12.dp))
                        .border(0.5.dp, Color(0xFFDADADA), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxSize()) {
                            Text(text = viewModel.selectDatePicker.value,
                                color = Color(0xFF6C7278),
                                fontSize = 10.sp,
                                fontFamily = FontFamily(Font(R.font.instrument_sans_regular)))
                            Image(painter = painterResource(id = R.drawable.ic_calender),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp))
                        }
                    }

                    // Time Picker Box
                    Box(modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clickable {
                            viewModel.showDatePicker = false
                            viewModel.showTimePicker = true
                        }
                        .background(Color(0xFFF6F6F6), RoundedCornerShape(12.dp))
                        .border(0.5.dp, Color(0xFFDADADA), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxSize()) {

                            Text(text = viewModel.selectTimePicker.value,
                                color = Color(0xFF6C7278),
                                fontSize = 10.sp,
                                fontFamily = FontFamily(Font(R.font.instrument_sans_regular)))
                            Image(painter = painterResource(id = R.drawable.ic_timer_set),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text(text = "Save",
                        color = Color(0xFFff6900),
                        modifier = Modifier
                            .clickable {
                                val dateValue = viewModel.selectDatePicker.value
                                val timeValue = viewModel.selectTimePicker.value
                                if (dateValue.contains("DD/MM/YYYY") || timeValue.contains("HH:MM:SS")) {
                                    showToastC(context, "Please select date and time")
                                    return@clickable
                                }

                                val item = SelectedRequestHolder.selectedItemList
                                Log.d("TAG",
                                    "ScheduleAppointment:Value  ${viewModel.selectDatePicker.value}--${viewModel.selectTimePicker.value}")
                                val model =
                                    SaveApppointmentRequest(convertDateTimeFormat(viewModel.selectDatePicker.value),
                                        viewModel.selectTimePicker.value,
                                        SharedPreference.get(context).userId.toInt(),
                                        item?.workid?.toInt() ?: 0)

                                // call schedule work appointment from viewmodel
                                viewModel.scheduleWorkAppointment(model, returnAccessToken(context))
                                onDismiss()
                            }
                            .padding(8.dp),
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.instrument_sans_medium)))
                }
            }
        }
    }
}