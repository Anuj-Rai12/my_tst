//package com.pay10.view.screens
//
//import android.Manifest
//import android.app.Activity
//import android.app.DatePickerDialog
//import android.app.TimePickerDialog
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.net.Uri
//import android.os.Build
//import android.util.Log
//import android.widget.Toast
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.horizontalScroll
//import androidx.compose.foundation.interaction.MutableInteractionSource
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.heightIn
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.layout.wrapContentSize
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.BasicTextField
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.Divider
//import androidx.compose.material3.Icon
//import androidx.compose.material3.OutlinedButton
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.livedata.observeAsState
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.rotate
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.toArgb
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalLifecycleOwner
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.Font
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.text.style.TextDecoration
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.window.Dialog
//import androidx.compose.ui.window.DialogProperties
//import androidx.core.app.ActivityCompat
//import androidx.core.view.WindowCompat
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.lifecycle.LifecycleOwner
//import androidx.navigation.NavHostController
//import com.fluper.curve_user_android.ui.m5.showDatePickerDialog
//import com.fluper.curve_user_android.ui.m5.showTimePickerDialog
//import com.pay10.R
//import com.pay10.domain.AuthViewModel
//import com.pay10.helper.CommonUtils
//import com.pay10.helper.CommonUtils.convertDateTimeFormat
//import com.pay10.helper.CommonUtils.convertDateTimeToDate
//import com.pay10.helper.CommonUtils.getLatLongFromAddress
//import com.pay10.helper.CommonUtils.returnAccessToken
//import com.pay10.helper.CommonUtils.showToastC
//import com.pay10.helper.CustomLoader
//import com.pay10.helper.EmpResource
//import com.pay10.helper.ErrorUtil
//import com.pay10.helper.SharedPreference
//import com.pay10.model.local.GetRequestList
//import com.pay10.model.local.UpdateRequest
//import com.pay10.view.MainActivity
//import com.pay10.view.SelectedRequestHolder
//import com.pay10.view.SingletonObject
//import com.pay10.view.location.DatabaseHelper
//import com.pay10.view.location.Position
//import com.pay10.view.navigation.Screen
//import kotlinx.coroutines.delay
//import java.util.Calendar
//import javax.inject.Singleton
//
//@Composable
//fun JobsScreen(navHostController: NavHostController,viewModel: AuthViewModel= hiltViewModel()){
//    val scroll = rememberScrollState()
////    val location by viewModel.locationState.collectAsState()
//    var search by remember { mutableStateOf("") }
//    var selectedIndex by remember { mutableStateOf(0) }
//    val items = listOf("All Jobs ", "Installation", "Complaint", "Paper Roll")
//    val context = LocalContext.current
//    (context as MainActivity).visibleStatusBar(context)
//    val lifecycleOwner = LocalLifecycleOwner.current
//    var  showDialog by remember { mutableStateOf(false) }
//    var  showDialogAppointment by remember { mutableStateOf(false) }
//    val calendar = Calendar.getInstance()
//    var selectedDatePicker by remember { mutableStateOf("DD/MM/YYYY") }
//    var selectedFromTime by remember { mutableStateOf("HH:MM:SS") }
//    val name =SharedPreference.get(context).name
//    val initial = name.firstOrNull()?.uppercase() ?: "?"
//    val activity = context as MainActivity
//
//    WindowCompat.setDecorFitsSystemWindows(activity.window, false)
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//        activity.window.setDecorFitsSystemWindows(false)
//        activity.window.statusBarColor = Color.Transparent.toArgb()
//        activity.window.navigationBarColor = Color.Transparent.toArgb()
//    } else {
//        @Suppress("DEPRECATION")
//        activity.window.decorView.systemUiVisibility =
//            activity.window.decorView.systemUiVisibility or
//                    android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//        @Suppress("DEPRECATION")
//        activity.window.statusBarColor = Color.Transparent.toArgb()
//    }
//    /*
//      var location1 by remember { mutableStateOf<Pair<Double, Double>?>(null) }
//        var loading by remember { mutableStateOf(true) }
//
//        LaunchedEffect(Unit) {
//            val dbHelper = DatabaseHelper(context)
//
//            while (true) {
//                dbHelper.selectPositionAsync(object : DatabaseHelper.DatabaseHandler<Position?> {
//                    override fun onComplete(success: Boolean, result: Position?) {
//                        if (success && result != null) {
//                            location1 = Pair(result.latitude, result.longitude)
//                            SharedPreference.get(context).latitude = result.latitude.toString()
//                            SharedPreference.get(context).longitude = result.longitude.toString()
//                            Log.d("TAG", "JobsScreen: Location - ${result.latitude} / ${result.longitude}")
//                        } else {
//                            Log.w("TAG", "Location not found or failed.")
//                        }
//                        loading = false
//                    }
//                })
//
//                delay(10_000) // Delay 10 seconds before the next fetch
//            }
//        }
//    */
//
//
//    Log.d("TAG", "JobsScreen:UserId ${SharedPreference.get(context).userId}")//userid is 41460
//
//    //current location updates......
//   /* val permissionLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestPermission()) { isGranted ->
//        if (isGranted) {
//            viewModel.startLocationUpdates()
//        } else {
//            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    LaunchedEffect(Unit) {
//        if (ActivityCompat.checkSelfPermission(
//                context,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
//        } else {
//            viewModel.startLocationUpdates()
//        }
//    }*/
//    //    Log.d("TAG", "JobsScreen: Loaction ---------${location?.latitude}----${location?.longitude}")
//
//
//    if(showDialog){
//        CustomDialogBoxLogout(context, viewModel, lifecycleOwner, navHostController) { result ->
//            showDialog = false
//            when (result) {
//                "yes" -> {
//                    Log.d("TAG", "Logout confirmed")
//                    SelectedRequestHolder.clearRequest()
//                    SingletonObject.clear()
//                    SharedPreference.get(context).accessToken=""
//                    SharedPreference.get(context).name=""
//                    SharedPreference.get(context).email=""
//                    SharedPreference.get(context).userId=""
//                    navHostController.navigate(Screen.LoginScreen.route) {
//                        popUpTo(Screen.JobsScreen.route) { inclusive = true }
//                    }
//                }
//                "no" -> {
//                    Log.d("TAG", "Logout cancelled")
//                    // No action needed, just dismiss
//                }
//                else -> {
//                    // Optional: Handle dismiss outside dialog
//                    Log.d("TAG", "Dialog dismissed without action")
//                }
//            }
//        }
//    }
//
//    if(showDialogAppointment){
//        ScheduleAppointment(viewModel){
//            showDialogAppointment = false
//            viewModel.showDatePicker =false
//            viewModel.showTimePicker=false
//        }
//    }
//
//    if(viewModel.showDatePicker){
//        if (viewModel.showDatePicker) {
//            showDatePickerDialog(
//                context = context,
//                onDateSelected = {selectedDate->
//                    viewModel.showDatePicker = false
//                    viewModel.showTimePicker=false
//                    viewModel.selectDatePicker.value = selectedDate
//                    selectedDatePicker = selectedDate
//                    viewModel.selectedDateLong.value = CommonUtils.convertDateToLong(selectedDate).toString()
//                    println("Selected Date: ${CommonUtils.convertDateToLong(selectedDate)}")
//                },
//                onDismiss = {
//                    viewModel.showDatePicker = false
//                    viewModel.showTimePicker=false
//                }
//            )
//        }
//    }
//
//    if(viewModel.showTimePicker){
//        showTimePickerDialog(
//            context = context,
//            onTimeSelected = {time->
//                viewModel.showTimePicker = false
//                viewModel.showDatePicker=false
//                viewModel.selectTimePicker.value = CommonUtils.convertTimestampToDateNew(time)
//                selectedFromTime = CommonUtils.convertTimestampToDateNew(time)
//                viewModel.selectedTimeLong.value =time.toString()
//                println("Selecte Time ${time}")
//            },
//            onDismiss = {
//                viewModel.showTimePicker = false // ✅ handles cancel/dismiss
//            }
//        )
//    }
//
//    LaunchedEffect(key1 = Unit) {
//        val model = GetRequestList("",0,0,"",0,CommonUtils.REQUESTTYPE.ALLJOBS.type,SharedPreference.get(context).name)
//        viewModel.hitRequestList(returnAccessToken(context),model)
//        observerList(context as MainActivity,viewModel,navHostController,lifecycleOwner)
//    }
//
//    val filteredList = viewModel.requestListData.filter { item ->
//        item.merchantName.contains(search, ignoreCase = true) ||
//                item.serialNo.contains(search, ignoreCase = true)
//    }
//
//    Brush.horizontalGradient(
//        listOf(Color(0xFFFFBB07), Color(0xFFEF1A23))
//    )
//    Column(modifier = Modifier.fillMaxSize()) {
//        Row(modifier = Modifier
//            .fillMaxWidth()
//            .background(color = Color(0XFFff6900))
//            .padding(top = 40.dp, bottom = 10.dp)
//            .padding(horizontal = 20.dp),
//            verticalAlignment = Alignment.CenterVertically) {
//
//           /* Image(painter = painterResource(R.drawable.ig),
//                contentDescription = "",
//                modifier = Modifier
//                    .width(34.dp)
//                    .clip(CircleShape),
//                    *//*.clickable {
//                       showDialog=true
//                    },*//*
//                contentScale = ContentScale.Crop,)*/
//
//            Box(
//                modifier = Modifier
//                    .size(34.dp)
//                    .background(Color.White, shape = CircleShape),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = initial,
//                    color = Color.Black,
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//
//            Spacer(Modifier.width(8.dp))
//            Text(
//                "${SharedPreference.get(context).name} \uD83D\uDC4B",
//                color = Color.White,
//                fontSize = 20.sp,
//                fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)))
//            Spacer(Modifier.width(10.dp))
//
//            Image(painter = painterResource(R.drawable.logout),
//                contentDescription = "",
//                modifier = Modifier
//                    .size(40.dp)
//                    .weight(1f)
//                    .clickable(
//                        indication = null,
//                        interactionSource = remember { MutableInteractionSource() }) {
//                        showDialog = true
//                    }, alignment = Alignment.CenterEnd)
//        }
//
//        // Scrollable content
//        Column(
//            modifier = Modifier
//                .weight(1f)
//                .verticalScroll(scroll)) {
//
//            Spacer(Modifier.height(20.dp))
//
//            Row(modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp)) {
//                BasicTextField(
//                    value = search,
//                    onValueChange = { search = it },
//                    textStyle = TextStyle(
//                        color = Color.Black,
//                        fontSize = 14.sp, // Match placeholder text
//                        fontFamily = FontFamily(Font(R.font.instrument_sans_regular))
//                    ),
//                    singleLine = true,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .border(width = 1.dp, color = Color(0XFFEAEEF0), shape = RoundedCornerShape(8.dp))
//                        .background(
//                            Color.White,
//                            shape = RoundedCornerShape(8.dp)
//                        ),
//                    decorationBox = { innerTextField ->
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp)
//                        ) {
//                            Image(
//                                painter = painterResource(R.drawable.ic_search_ic),
//                                contentDescription = "Search",
//                                modifier = Modifier.size(20.dp)
//                            )
//
//                            Spacer(Modifier.width(8.dp))
//
//                            Box(
//                                modifier = Modifier
//                                    .weight(1f),
//                                contentAlignment = Alignment.CenterStart
//                            ) {
//                                if (search.isEmpty()) {
//                                    Text(
//                                        text = "Search merchants...",
//                                        color = Color(0XFF35494F),
//                                        fontSize = 14.sp, // match here
//                                        fontFamily = FontFamily(Font(R.font.instrument_sans_regular))
//                                    )
//                                }
//                                innerTextField()
//                            }
//
//                            if (search.isNotEmpty()) {
//                                Spacer(Modifier.width(8.dp))
//                                Icon(
//                                    painter = painterResource(R.drawable.close_ic),
//                                    contentDescription = "Clear search",
//                                    tint = Color.Gray,
//                                    modifier = Modifier
//                                        .size(18.dp)
//                                        .clickable { search = "" }
//                                )
//                            }
//                        }
//                    }
//                )
//            }
//            Spacer(Modifier.height(10.dp))
//
//            Card(
//                shape = RoundedCornerShape(6.dp),
//                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//                colors = CardDefaults.cardColors(containerColor = Color.White),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 8.dp, start = 16.dp, end = 16.dp)
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    DashboardCard(
//                        title = "Installation",
//                        count = viewModel.saveDataRequest?.installation ?: 0,
//                        iconRes = R.drawable.install_third,
//                        modifier = Modifier.weight(1f)
//                    )
//                    DashboardCard(
//                        title = "Complaint",
//                        count = viewModel.saveDataRequest?.complains ?: 0,
//                        iconRes = R.drawable.complaint_second,
//                        modifier = Modifier.weight(1f)
//                    )
//                    DashboardCard(
//                        title = "Pending",
//                        count = viewModel.saveDataRequest?.paperRoll ?: 0,
//                        iconRes = R.drawable.paperroll_second,
//                        modifier = Modifier.weight(1f)
//                    )
//                }
//            }
//
//            Spacer(Modifier.height(8.dp))
//
//            Row(
//                modifier = Modifier
//                    .padding(start = 16.dp, end = 16.dp, top = 8.dp)
//                    .horizontalScroll(rememberScrollState())
//            ) {
//                items.forEachIndexed { index, item ->
//                    val isSelected = index == selectedIndex
//
//                    Box(
//                        modifier = Modifier
//                            .padding(end = 8.dp)
//                            .border(
//                                width = 1.dp,
//                                color = if (isSelected) Color(0xFFff6900) else Color.Gray,
//                                shape = RoundedCornerShape(8.dp))
//
//                            .background(
//                                color = Color.White,
//                                shape = RoundedCornerShape(8.dp))
//
//                            .clickable(
//                                indication = null,
//                                interactionSource = remember { MutableInteractionSource() }) {
//                                selectedIndex = index
//                                var type = CommonUtils.REQUESTTYPE.ALLJOBS.type
//                                when (index) {
//                                    0 -> type = CommonUtils.REQUESTTYPE.ALLJOBS.type
//                                    1 -> type = CommonUtils.REQUESTTYPE.INSTALLATION.type
//                                    2 -> type = CommonUtils.REQUESTTYPE.COMPLAINT.type
//                                    3 -> type = CommonUtils.REQUESTTYPE.PAPERROLL.type
//                                }
//                                val model = GetRequestList(
//                                    "",
//                                    0,
//                                    0,
//                                    "",
//                                    type,
//                                    0,
//                                    SharedPreference.get(context).name
//                                )
//                                viewModel.hitRequestList(returnAccessToken(context), model)
//                            }
//                            .padding(horizontal = 8.dp, vertical = 8.dp),
//                        contentAlignment = Alignment.Center) {
//                        Text(text = item,
//                            fontSize = 14.sp,
//                            color = if (isSelected) Color(0xFFff6900) else Color.Gray,
//                            textAlign = TextAlign.Center)
//                    }
//                }
//            }
//            Spacer(Modifier.height(8.dp))
//
//            if (filteredList.isNotEmpty()) {
//
//                LazyColumn(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 30.dp)
//                        .heightIn(max = 500.dp)
//                ) {
//                    items(filteredList.size) { index ->
//                        val item = filteredList[index]
//                        val itemChecklist = viewModel.requestListChecklistData
//
//                        Spacer(modifier = Modifier.height(8.dp))
//
//                        Card(
//                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//                            shape = RoundedCornerShape(8.dp),
//                            colors = CardDefaults.cardColors(
//                                containerColor = Color.White // 👈 white background
//                            ),
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(bottom = 8.dp)
//                        ) {
//                            Column(
//                                modifier = Modifier
//                                    .padding(12.dp),
//                                verticalArrangement = Arrangement.spacedBy(2.dp)) {
//                                // Top Row: Merchant Name + Status
//                                Row(
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
//                                        Text(
//                                            text = item.merchantName,
//                                            fontSize = 12.sp,
//                                            color = Color.Black,
//                                            fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold))
//                                        )
//                                        Text(
//                                            text = "(${item.merchantCode})",
//                                            fontFamily = FontFamily(Font(R.font.instrument_sans_medium)),
//                                            fontSize = 12.sp,
//                                            color = Color.Black
//                                        )
//                                    }
//
//                                    Spacer(Modifier.weight(1f))
//
//                                    Row(){
//                                        Text(
//                                            text = "Status: ",
//                                            fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)),
//                                            fontSize = 12.sp,
//                                            color =Color.Black)
//                                        Text(
//                                            text = "${item.status}",
//                                            fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)),
//                                            fontSize = 12.sp,
//                                            color = when (item.status.lowercase()) {
//                                                "completed" -> Color(0xFF4CAF50) // Green
//                                                "assigned" -> Color.Black
//                                                "progress", "in progress" -> Color(0xFFff6900) // Orange
//                                                else -> Color.Gray
//                                            }
//                                        )
//                                    }
//                                }
//
//                                // Serial Number
//                                Text(text = "S.No: ${item.serialNo}",
//                                    fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
//                                    fontSize = 12.sp,
//                                    color = Color.Black)
//
//                                Row(
//                                    verticalAlignment = Alignment.CenterVertically,
//                                    modifier = Modifier.fillMaxWidth()) {
//                                    Image(painter = painterResource(R.drawable.location_pin_grey),
//                                        contentDescription = null,
//                                        modifier = Modifier.size(14.dp))
//
//                                    Spacer(modifier = Modifier.width(4.dp))
//
//                                    Text(
//                                        text = item.location,
//                                        fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
//                                        fontSize = 12.sp,
//                                        color = Color.Black,
//                                        modifier = Modifier.weight(1f))
//
//                                   /* Text(
//                                        text = "Navigate",
//                                        fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
//                                        fontSize = 12.sp,
//                                        color = Color(0xFFff6900),
//                                        textDecoration = TextDecoration.Underline,
//                                        modifier = Modifier
//                                            .clickable {
//                                                val originLat=SharedPreference.get(context).latitude
//                                                val originLong=SharedPreference.get(context).longitude
//                                                val destination = getLatLongFromAddress(context, item.location)
//                                                val uri = Uri.parse(
//                                                    "https://www.google.com/maps/dir/?api=1&origin=${originLat.toDouble()},${originLong.toDouble()}&destination=${destination?.first},${destination?.second}&travelmode=driving"
//                                                )
//                                                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
//                                                    setPackage("com.google.android.apps.maps")
//                                                }
//                                                if (intent.resolveActivity(context.packageManager) != null) {
//                                                    context.startActivity(intent)
//                                                } else {
//                                                    Toast.makeText(context, "Google Maps not installed", Toast.LENGTH_SHORT).show()
//                                                }
//                                            }
//                                    )*/
//                                }
//
//                                // Device and View Button
//                                Row(
//                                    modifier = Modifier.fillMaxWidth(),
//                                    horizontalArrangement = Arrangement.SpaceBetween,
//                                    verticalAlignment = Alignment.CenterVertically) {
//                                    Row(verticalAlignment = Alignment.CenterVertically) {
//                                        Text(
//                                            text = "Device: ",
//                                            fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
//                                            fontSize = 12.sp,
//                                            color = Color.Black)
//
//                                        Text(
//                                            text = item.deviceType,
//                                            fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
//                                            fontSize = 12.sp,
//                                            color = Color.Black)
//                                    }
//
//                                    Box(
//                                        modifier = Modifier
//                                            .clip(RoundedCornerShape(8.dp))
//                                            .background(
//                                                Brush.horizontalGradient(
//                                                    colors = listOf(
//                                                        Color(0xFFff6900),
//                                                        Color(0xFFFF6600)
//                                                    )
//                                                )
//                                              /*  Brush.horizontalGradient(
//                                                    colors = listOf(
//                                                        Color(0xFFFFBB07),
//                                                        Color(0xFFEF1A23)
//                                                    )
//                                                )*/
//                                            )
//                                            .clickable {
//                                                if (item.status.contains("Failed")) {
//                                                    showToastC(
//                                                        context,
//                                                        "This request is failed, please contact admin."
//                                                    )
//                                                }else if(item.status.contains("Completed")){
//                                                    SelectedRequestHolder.selectedItemList = item
//                                                    SelectedRequestHolder.selectedItemCheckList = itemChecklist
//                                                    when {
//                                                        item.requesttype.contains("Complaint") -> {
//                                                            navHostController.navigate(Screen.ComplaintScreen.route)
//                                                        }
//
//                                                        item.requesttype.contains("PaperRoll") -> {
//                                                            navHostController.navigate(Screen.PaperRollScreen.route)
//                                                        }
//
//                                                        else -> {
//                                                            navHostController.navigate(Screen.JobInstallationDetailScreen.route)
//                                                        }
//                                                    }
//                                                }
//                                                else {
//                                                    if(item.appointmentDate.equals("")){
//                                                        showToastC(context, "Please take appointment first")
//                                                        return@clickable
//                                                    }
//                                                    SelectedRequestHolder.selectedItemList = item
//                                                    SelectedRequestHolder.selectedItemCheckList = itemChecklist
//                                                    when {
//                                                        item.requesttype.contains("Complaint") -> {
//                                                            navHostController.navigate(Screen.ComplaintScreen.route)
//                                                        }
//
//                                                        item.requesttype.contains("PaperRoll") -> {
//                                                            navHostController.navigate(Screen.PaperRollScreen.route)
//                                                        }
//
//                                                        else -> {
//                                                            navHostController.navigate(Screen.JobInstallationDetailScreen.route)
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                            .padding(horizontal = 12.dp, vertical = 4.dp),
//                                        contentAlignment = Alignment.Center
//                                    ) {
//                                        Text(
//                                            text = "View",
//                                            color = Color.White,
//                                            fontSize = 12.sp,
//                                            fontWeight = FontWeight.Medium,
//                                            fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold))
//                                        )
//                                    }
//                                }
//
//                                if(item.status.contains(CommonUtils.STATUS.COMPLETED.sttausname) || item.status.contains(CommonUtils.STATUS.FAILED.sttausname)){
//
//                                }else{
//                                    if(item.appointmentDate != ""){
//                                        Text(
//                                            text = "Date: ${convertDateTimeToDate(item.appointmentDate)},${item.appointmentTime}",
//                                            color = Color(0xFF000000),
//                                            fontSize = 12.sp,
//                                            fontFamily = FontFamily(Font(R.font.instrument_sans_regular))
//                                        )
//                                    }else {
//                                        Text(
//                                            text = "Take Appointment",
//                                            color = Color(0xFFff6900),
//                                            fontSize = 12.sp,
//                                            modifier = Modifier.clickable {
//                                                SelectedRequestHolder.selectedItemList = item
//                                                showDialogAppointment = true
//                                                viewModel.selectDatePicker.value = "DD/MM/YYYY"
//                                                viewModel.selectTimePicker.value = "HH:MM:SS"
//                                            },
//                                            textDecoration = TextDecoration.Underline,
//                                            fontFamily = FontFamily(Font(R.font.instrument_sans_regular))
//                                        )
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//
//            }else{
//                Box(modifier = Modifier
//                    .fillMaxWidth()
//                    .height(300.dp),
//                    contentAlignment = Alignment.Center) {
//                    Text(text = "No data found",
//                        fontSize = 16.sp,
//                        fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)),
//                        color = Color.Black)
//                }
//            }
//        }
//    }
//}
//
//fun observerList(
//    context: MainActivity,
//    viewModel: AuthViewModel,
//    navController: NavHostController?,
//    lifecycleOwner: LifecycleOwner, ) {
//    viewModel.requestListLiveData.observe(lifecycleOwner){
//        when(it){
//            is EmpResource.Failure ->{
//                it.throwable?.let { it1 -> ErrorUtil.handlerGeneralError(context, it1) }
//                CustomLoader.hideLoader()
//                if( it.throwable?.message?.contains("401") == true) {
//                    Log.d("TAG", "observerList: Unauthorized access, navigating to login")
//                    SharedPreference.get(context).accessToken = ""
//                    SharedPreference.get(context).name = ""
//                    SharedPreference.get(context).email = ""
//                    SharedPreference.get(context).userId = ""
//                    navController?.navigate(Screen.LoginScreen.route) {
//                        popUpTo(Screen.JobsScreen.route) { inclusive = true }
//                    }
//                }
//            }
//
//            EmpResource.Loading -> {
//                CustomLoader.showLoader(context)
//            }
//
//            is EmpResource.Success -> {
//                CustomLoader.hideLoader()
//                viewModel.saveDataRequest=it.value.data
//                viewModel.requestListData =it.value.data.requestList
//                viewModel.requestListChecklistData =it.value.data.checklist
//            }
//        }
//    }
//
//    viewModel.updateLiveData.observe(lifecycleOwner) {
//        when (it) {
//            is EmpResource.Failure -> {
//                it.throwable?.let { it1 -> ErrorUtil.handlerGeneralError(context, it1) }
//                CustomLoader.hideLoader()
//            }
//
//            EmpResource.Loading -> {
//                CustomLoader.showLoader(context)
//            }
//
//            is EmpResource.Success -> {
//                CustomLoader.hideLoader()
//                showToastC(context,"Scheduled appointment successfully")
//                val model = GetRequestList("",0,0,"",0,CommonUtils.REQUESTTYPE.ALLJOBS.type,SharedPreference.get(context).name)
//                viewModel.hitRequestList(returnAccessToken(context),model)
//            }
//        }
//    }
//}
//
//@Composable
//fun CustomDialogBoxLogout(
//    context: Activity,
//    viewModel: AuthViewModel,
//    lifecycleOwner: LifecycleOwner,
//    navHostController: NavHostController,
//    onResult: (String) -> Unit
//) {
//    Dialog(onDismissRequest = { onResult("cancel") }) {
//        Surface(
//            shape = RoundedCornerShape(12.dp),
//            color = Color.White,
//            tonalElevation = 8.dp,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 24.dp)) {
//
//
//            Column(modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 20.dp),
//                horizontalAlignment = Alignment.CenterHorizontally)
//            {
//                Image(
//                    painter = painterResource(id = R.drawable.ic_logout),
//                    contentDescription = null,
//                    modifier = Modifier.size(66.dp))
//
//                Spacer(modifier = Modifier.height(16.dp))
//                Text(text = "Are you sure you want to logout?",
//                    fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)),
//                    fontSize = 15.sp,
//                    textAlign = TextAlign.Center,
//                    modifier = Modifier.padding(horizontal = 16.dp),
//                    color = Color(0xff0E1C21))
//
//                Spacer(Modifier.height(12.dp))
//                Row(modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 20.dp, vertical = 16.dp),
//                    horizontalArrangement = Arrangement.spacedBy(12.dp),
//                    verticalAlignment = Alignment.CenterVertically) {
//                    // Yes Button
//                    Box(
//                        modifier = Modifier
//                            .weight(1f)
//                            .border(1.dp, Color(0xFFA8824F), RoundedCornerShape(26.dp))
//                            .background(Color.White, RoundedCornerShape(26.dp))
//                            .clickable { onResult("yes") }
//                            .padding(vertical = 12.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = "Yes",
//                            fontSize = 16.sp,
//                            color = Color(0xFFA8824F),
//                            fontFamily = FontFamily(Font(R.font.instrument_sans_medium))
//                        )
//                    }
//
//                    // No Button
//                    Box(
//                        modifier = Modifier
//                            .weight(1f)
//                            .background(Color(0xFF003A4E), RoundedCornerShape(26.dp))
//                            .clickable { onResult("no") }
//                            .padding(vertical = 12.dp),
//                        contentAlignment = Alignment.Center) {
//                        Text(
//                            text = "No",
//                            fontSize = 16.sp,
//                            color = Color.White,
//                            fontFamily = FontFamily(Font(R.font.instrument_sans_medium))
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun ScheduleAppointment(
//    viewModel: AuthViewModel,
//    onDismiss: () -> Unit) {
//
//    val context = LocalContext.current
//    Dialog(
//        onDismissRequest = { onDismiss()
//            viewModel.showDatePicker=false
//            viewModel.showTimePicker=false
//                           },
//        properties = DialogProperties(
//            dismissOnClickOutside = true  // ✅ makes dialog cancelable on outside tap
//        )
//    ) {
//        Surface(
//            shape = RoundedCornerShape(16.dp),
//            color = Color.White,
//            tonalElevation = 8.dp,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(20.dp)) {
//
//                Text(
//                    text = "Schedule Appointment",
//                    color = Color(0xFF333333),
//                    fontSize = 12.sp,
//                    modifier = Modifier.fillMaxWidth(),
//                    textAlign = TextAlign.Center,
//                    fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold))
//                )
//                Spacer(Modifier.height(8.dp))
//                Row(
//                    modifier = Modifier.fillMaxWidth(),) {
//                    Text(
//                        text = "Select Date",
//                        color = Color(0xFF333333),
//                        fontSize = 12.sp,
//                        textAlign = TextAlign.Start,
//                        fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold))
//                    )
//                    Spacer(modifier = Modifier.width(24.dp))
//                    Text(
//                        text = "Select Time",
//                        color = Color(0xFF333333),
//                        fontSize = 12.sp,
//                        modifier = Modifier.padding(start = 28.dp),
//                        textAlign = TextAlign.Center,
//                        fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold))
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                // Date & Time Pickers Row
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//
//                    // Date Picker Box
//                    Box(
//                        modifier = Modifier
//                            .weight(1f)
//                            .height(50.dp)
//                            .clickable {
//                                viewModel.showTimePicker = false
//                                viewModel.showDatePicker = true
//                            }
//                            .background(Color(0xFFF6F6F6), RoundedCornerShape(12.dp))
//                            .border(0.5.dp, Color(0xFFDADADA), RoundedCornerShape(12.dp))
//                            .padding(horizontal = 12.dp, vertical = 8.dp)
//                    ) {
//                        Row(
//                            horizontalArrangement = Arrangement.SpaceBetween,
//                            verticalAlignment = Alignment.CenterVertically,
//                            modifier = Modifier.fillMaxSize()
//                        ) {
//                            Text(
//                                text = viewModel.selectDatePicker.value,
//                                color = Color(0xFF6C7278),
//                                fontSize = 10.sp,
//                                fontFamily = FontFamily(Font(R.font.instrument_sans_regular))
//                            )
//                            Image(
//                                painter = painterResource(id = R.drawable.ic_calender),
//                                contentDescription = null,
//                                modifier = Modifier.size(18.dp)
//                            )
//                        }
//                    }
//
//                    // Time Picker Box
//                    Box(
//                        modifier = Modifier
//                            .weight(1f)
//                            .height(50.dp)
//                            .clickable {
//                                viewModel.showDatePicker = false
//                                viewModel.showTimePicker = true
//                            }
//                            .background(Color(0xFFF6F6F6), RoundedCornerShape(12.dp))
//                            .border(0.5.dp, Color(0xFFDADADA), RoundedCornerShape(12.dp))
//                            .padding(horizontal = 12.dp, vertical = 8.dp)
//                    ) {
//                        Row(
//                            horizontalArrangement = Arrangement.SpaceBetween,
//                            verticalAlignment = Alignment.CenterVertically,
//                            modifier = Modifier.fillMaxSize()
//                        ) {
//                            Text(
//                                text = viewModel.selectTimePicker.value,
//                                color = Color(0xFF6C7278),
//                                fontSize = 10.sp,
//                                fontFamily = FontFamily(Font(R.font.instrument_sans_regular))
//                            )
//                            Image(
//                                painter = painterResource(id = R.drawable.ic_timer_set),
//                                contentDescription = null,
//                                modifier = Modifier.size(18.dp)
//                            )
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(20.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.End) {
//                    Text(
//                        text = "Save",
//                        color = Color(0xFFff6900),
//                        modifier = Modifier
//                            .clickable {
//                                if(viewModel.selectDatePicker.value.contains("DD/MM/YYYY") && viewModel.selectTimePicker.value.contains("HH:MM:SS")){
//                                    showToastC(context,"Please select date and time")
//                                    return@clickable
//                                }
//                                 val item  = SelectedRequestHolder.selectedItemList
//                                val model = UpdateRequest(
//                                    "",
//                                    "1900-01-01",
//                                    false,
//                                    "",
//                                    SharedPreference.get(context).userId.toInt(),
//                                    0,
//                                    0,
//                                    item?.requestid?.toInt() ?: 0,
//                                    CommonUtils.STATUS.ASSIGNED.type,
//                                    0,
//                                    convertDateTimeFormat(viewModel.selectDatePicker.value),
//                                    viewModel.selectTimePicker.value)
//                                viewModel.hitUpdate(returnAccessToken(context), model)
//                                onDismiss()
//                            }
//                            .padding(8.dp),
//                        fontSize = 14.sp,
//                        fontFamily = FontFamily(Font(R.font.instrument_sans_medium))
//                    )
//                }
//            }
//        }
//    }
//}
//
//
//@Composable
//fun DashboardCard(
//    title: String,
//    count: Int,
//    iconRes: Int,
//    modifier: Modifier = Modifier
//) {
//    Column(
//        modifier = modifier
//            .padding(vertical = 10.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Box(
//            modifier = Modifier
//                .size(38.dp)
//                .clip(CircleShape)
//                .background(color = Color(0xFFF6F6F6))
//        ) {
//            Image(
//                painter = painterResource(iconRes),
//                contentDescription = null,
//                modifier = Modifier
//                    .align(Alignment.Center)
//                    .size(28.dp)
//            )
//        }
//
//        Spacer(modifier = Modifier.height(6.dp))
//
//        Text(
//            text = "$title($count)",
//            fontSize = 12.sp,
//            fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
//            color = Color(0XFF000000)
//        )
//    }
//}
//
///*@Composable
//fun DashboardCard(
//    title: String,
//    count: Int,
//    iconRes: Int
//) {
//    val cardWidth = 100.dp // Reduced to fit 3 cards
//
//    Card(
//        shape = RoundedCornerShape(6.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        modifier = Modifier
//            .width(cardWidth)
//            .padding(top = 8.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 10.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Box(
//                modifier = Modifier.size(38.dp) // Smaller icon container
//            ) {
//                Image(
//                    painter = painterResource(iconRes),
//                    contentDescription = "",
//                    modifier = Modifier
//                        .align(Alignment.Center)
//                        .size(28.dp) // Smaller icon
//                )
//            }
//
//            Spacer(modifier = Modifier.height(6.dp))
//
//            Text(
//                text = "$title($count)",
//                fontSize = 12.sp,
//                fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
//                color = Color(0XFF000000)
//            )
//        }
//    }
//}*/
//
//
//
//
//
