package com.pos10.view.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.fluper.curve_user_android.ui.m5.showDatePickerDialogWithoutDueDate
import com.pos10.R
import com.pos10.db.database.AppDatabase
import com.pos10.domain.AuthViewModel
import com.pos10.helper.CommonUtils
import com.pos10.helper.CommonUtils.convertDateFormat
import com.pos10.helper.CommonUtils.returnAccessToken
import com.pos10.helper.CommonUtils.showToastC
import com.pos10.helper.CustomLoader
import com.pos10.helper.EmpResource
import com.pos10.helper.ErrorUtil
import com.pos10.helper.SharedPreference
import com.pos10.model.local.GetAgenTrackHistoryRequest
import com.pos10.model.local.NotificationToggleRequest
import com.pos10.view.CoordinateManager
import com.pos10.view.MainActivity
import com.pos10.view.SelectedRequestHolder
import com.pos10.view.SingletonObject
import com.pos10.view.navigation.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SideProfileScreen(navHostController: NavHostController,
                      viewModel: AuthViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val toggles = remember { mutableStateListOf(*Array(2) { true }) }
    var showDialog by remember { mutableStateOf(false) }
    var showDialogTrackHistory by remember { mutableStateOf(false) }
    var selectedDatePicker by remember { mutableStateOf("DD/MM/YYYY") }
    var selectedDateEndPicker by remember { mutableStateOf("DD/MM/YYYY") }
    val lifecycleOwner = LocalLifecycleOwner.current
    val isNotificationEnabled = remember {
        mutableStateOf(SharedPreference.get(context).isNotificationStatus)
    }

    LaunchedEffect(key1 = Unit) {
        observerHistroy(context as MainActivity, viewModel, navHostController, lifecycleOwner)
    }

    if (showDialog) {
        CustomDialogBoxLogout(context as MainActivity,
            viewModel,
            lifecycleOwner,
            navHostController) { result ->
            showDialog = false
            when (result) {
                "yes" -> {
                    val db = AppDatabase.getInstance(context)
                    CoroutineScope(Dispatchers.IO).launch {
                        // Clear all tables in Room DB
                        db.clearAllTables()
                    }
                    Log.d("TAG", "Logout confirmed")
                    SelectedRequestHolder.clearRequest()
                    SingletonObject.clear()
                    SharedPreference.get(context).accessToken = ""
                    SharedPreference.get(context).name = ""
                    SharedPreference.get(context).email = ""
                    SharedPreference.get(context).userId = ""
                    navHostController.navigate(Screen.LoginScreen.route) {
                        popUpTo(Screen.JobsScreen.route) { inclusive = true }
                    }
                }

                "no" -> {
                    Log.d("TAG", "Logout cancelled")
                    // No action needed, just dismiss
                }

                else -> {
                    // Optional: Handle dismiss outside dialog
                    Log.d("TAG", "Dialog dismissed without action")
                }
            }
        }
    }

    if (showDialogTrackHistory) {
        ShowLivetrackingHistory(viewModel) {
            showDialogTrackHistory = false
            viewModel.showDatePickerHistory = false
        }
    }

    if (viewModel.showDatePickerHistory) {
        showDatePickerDialogWithoutDueDate(context = context, onDateSelected = { selectedDate ->
            viewModel.showDatePickerHistory = false
            viewModel.showDatePickerEndHistory = false
            viewModel.selectDatePickerStartDate.value = selectedDate
            selectedDatePicker = selectedDate
            viewModel.selectedStartDateLong.value =
                CommonUtils.convertDateToLong(selectedDate).toString()
        }, onDismiss = {
            viewModel.showDatePickerHistory = false
            viewModel.showDatePickerEndHistory = false
        })
    }

    if (viewModel.showDatePickerEndHistory) {
        showDatePickerDialogWithoutDueDate(context = context, onDateSelected = { selectedDate ->
            viewModel.showDatePickerEndHistory = false
            viewModel.showDatePickerHistory = false
            viewModel.selectDatePickerEndDate.value = selectedDate
            selectedDateEndPicker = selectedDate
            viewModel.selectedEndDateLong.value =
                CommonUtils.convertDateToLong(selectedDate).toString()
        }, onDismiss = {
            viewModel.showDatePickerHistory = false
            viewModel.showDatePickerEndHistory = false
        })
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0XFFFFFFFF))
                .padding(top = 50.dp, bottom = 10.dp)
                .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(R.drawable.logo_pos10),
                    contentDescription = "",
                    modifier = Modifier
                        .width(80.dp)
                        .height(50.dp))
            }

            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(start = 0.dp, end = 0.dp, bottom = 12.dp)
                .background(Color(0xFFF8FBFF))) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Profile Row
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 12.dp, end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Image(painter = painterResource(R.drawable.profile),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp))

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(text = SharedPreference.get(context).name,
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.instrument_sans_medium)))
                            Text(text = SharedPreference.get(context).email,
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 0.dp),
                                fontFamily = FontFamily(Font(R.font.instrument_sans_regular)))
                        }
                    }

                    // Divider
                    Divider(color = Color.LightGray,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 24.dp))

                    // Rating Row
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 16.dp)
                        .clickable(indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            navHostController.navigate(Screen.FeedbackScreen.route)
                        }, verticalAlignment = Alignment.CenterVertically) {
                        Image(painter = painterResource(R.drawable.rating_star_yellow_fill),
                            modifier = Modifier.size(12.dp),
                            contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(text = "${SharedPreference.get(context).averageRating} My Rating",
                            fontSize = 14.sp,
                            fontFamily = FontFamily(Font(R.font.instrument_sans_medium)))
                        Spacer(Modifier.weight(1f))
                        Image(painter = painterResource(R.drawable.ic_right_arrow),
                            modifier = Modifier.size(12.dp),
                            contentDescription = null)
                    }
                }
            }

            Column(modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White)) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 16.dp, top = 16.dp)
                    .clickable(indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {
                        showToastC(context, "Contact to admin")
                    }, verticalAlignment = Alignment.CenterVertically) {
                    Image(painter = painterResource(R.drawable.ic_transaction),
                        modifier = Modifier.size(22.dp),
                        contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(text = "Support",
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.instrument_sans_medium)))
                    Spacer(Modifier.weight(1f))
                    Image(painter = painterResource(R.drawable.ic_right_arrow),
                        modifier = Modifier.size(12.dp),
                        contentDescription = null)
                }
                Spacer(Modifier.height(12.dp))

                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 16.dp, top = 16.dp)
                    .clickable(indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {
                        showDialogTrackHistory = true
                    }, verticalAlignment = Alignment.CenterVertically) {
                    Image(painter = painterResource(R.drawable.ic_transaction),
                        modifier = Modifier.size(22.dp),
                        contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(text = "History",
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.instrument_sans_medium)))
                    Spacer(Modifier.weight(1f))
                    Image(painter = painterResource(R.drawable.ic_right_arrow),
                        modifier = Modifier.size(12.dp),
                        contentDescription = null)
                }
                Spacer(Modifier.height(12.dp))

                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 16.dp, top = 16.dp)
                    .clickable(indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {},
                    verticalAlignment = Alignment.CenterVertically) {
                    Image(painter = painterResource(R.drawable.ic_notifications),
                        modifier = Modifier.size(22.dp),
                        contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(text = "Notifications",
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.instrument_sans_medium)))
                    Spacer(Modifier.weight(1f))
                    Image(
                        painter = painterResource(
                            if (isNotificationEnabled.value)
                                R.drawable.ic_toggle_on
                            else
                                R.drawable.ic_toggle_off
                        ),
                        modifier = Modifier
                            .size(22.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                // toggle local state
                                val newValue = !isNotificationEnabled.value
                                isNotificationEnabled.value = newValue

                                // save in shared pref
                                SharedPreference.get(context).isNotificationStatus = newValue

                                Log.d("TAG", "SideProfileScreen:Noti --- $newValue")

                                // call API
                                val model = NotificationToggleRequest(
                                    newValue,
                                    SharedPreference.get(context).userId.toInt()
                                )
                                viewModel.hitNotificationToggle(returnAccessToken(context), model)
                            },
                        contentDescription = null
                    )
                }

                Spacer(Modifier.height(16.dp))

                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .clickable {
                        showDialog = true
                    }, contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Color(0xFFff6900), shape = RoundedCornerShape(8.dp))
                        .padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                        Text(text = "Logout",
                            fontSize = 16.sp,
                            color = Color(0xFFFFFFFF),
                            fontFamily = FontFamily(Font(R.font.instrument_sans_medium)))
                    }
                }
            }
        }
    }
}

@Composable
fun ShowLivetrackingHistory(viewModel: AuthViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    Dialog(onDismissRequest = {
        onDismiss()
        viewModel.showDatePickerHistory = false
    },
        properties = DialogProperties(dismissOnClickOutside = true  // ✅ makes dialog cancelable on outside tap
        )) {
        Surface(shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)) {

                Text(text = "Show Track History",
                    color = Color(0xFF333333),
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)))
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Start Date",
                        color = Color(0xFF333333),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Start,
                        fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)))
                    Spacer(modifier = Modifier.width(24.dp))
                    Text(text = "End Date",
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
                            viewModel.showDatePickerHistory = true
                            viewModel.showDatePickerEndHistory = false

                        }
                        .background(Color(0xFFF6F6F6), RoundedCornerShape(12.dp))
                        .border(0.5.dp, Color(0xFFDADADA), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxSize()) {
                            Text(text = viewModel.selectDatePickerStartDate.value,
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
                            viewModel.showDatePickerHistory = false
                            viewModel.showDatePickerEndHistory = true
                        }
                        .background(Color(0xFFF6F6F6), RoundedCornerShape(12.dp))
                        .border(0.5.dp, Color(0xFFDADADA), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxSize()) {
                            Text(text = viewModel.selectDatePickerEndDate.value,
                                color = Color(0xFF6C7278),
                                fontSize = 10.sp,
                                fontFamily = FontFamily(Font(R.font.instrument_sans_regular)))
                            Image(painter = painterResource(id = R.drawable.ic_calender),
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
                                Log.d("TAG",
                                    "ShowLivetrackingHistory:Check ---${viewModel.selectDatePickerStartDate.value} ")

                                val dateStartValue = viewModel.selectDatePickerStartDate.value
                                val dateEndValue = viewModel.selectDatePickerEndDate.value

                                if (dateStartValue.contains("DD/MM/YYYY") || dateEndValue.contains("DD/MM/YYYY")) {
                                    showToastC(context, "Please select date")
                                    return@clickable
                                }

                                val startDate = convertDateFormat(dateStartValue)
                                val endDate = convertDateFormat(dateEndValue)
                                val model =
                                    GetAgenTrackHistoryRequest(SharedPreference.get(context).userId,
                                        endDate,
                                        startDate,
                                        "0")
                                viewModel.hitHistoryTrack(returnAccessToken(context), model)
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

fun observerHistroy(
    context: MainActivity,
    viewModel: AuthViewModel,
    navController: NavHostController?,
    lifecycleOwner: LifecycleOwner,
) {
    viewModel.historytrackLiveData.observe(lifecycleOwner) {
        when (it) {
            is EmpResource.Failure -> {
                it.throwable?.let { it1 -> ErrorUtil.handlerGeneralError(context, it1) }
                CustomLoader.hideLoader()
            }

            EmpResource.Loading -> {
                CustomLoader.showLoader(context)
            }

            is EmpResource.Success -> {
                CustomLoader.hideLoader()
                if (it.value.info.isSuccess) {
                    val apiData = it.value.data
                    CoordinateManager.setCoordinatesFromApi(apiData)
                    navController?.navigate(Screen.RouteTrackHistry.route)
                    Log.d("RouteCoordinates", CoordinateManager.getCoordinates().toString())
                } else {
                    showToastC(context, it.value.info.message)
                }
            }
        }
    }

    viewModel.notifictionStatusLiveData.observe(lifecycleOwner) {
        when (it) {
            is EmpResource.Failure -> {
                it.throwable?.let { it1 -> ErrorUtil.handlerGeneralError(context, it1) }
            }

            EmpResource.Loading -> {
                CustomLoader.showLoader(context)
            }

            is EmpResource.Success -> {
                CustomLoader.hideLoader()
                SharedPreference.get(context).isNotificationStatus=it.value.data.isNotificationOn
            }
        }
    }
}




