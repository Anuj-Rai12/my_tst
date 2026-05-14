package com.pos10.view.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.assetinfinity.app.gpsclient.DatabaseHelper
import com.assetinfinity.app.gpsclient.Position
import com.fluper.curve_user_android.ui.m5.showDatePickerDialog
import com.fluper.curve_user_android.ui.m5.showTimePickerDialog
import com.google.gson.Gson
import com.pos10.R
import com.pos10.domain.AuthViewModel
import com.pos10.helper.CommonUtils
import com.pos10.helper.CommonUtils.parseDateToMillis
import com.pos10.helper.CommonUtils.returnAccessToken
import com.pos10.helper.CommonUtils.showToastC
import com.pos10.helper.CustomLoader
import com.pos10.helper.EmpResource
import com.pos10.helper.ErrorUtil
import com.pos10.helper.SharedPreference
import com.pos10.model.local.UpdateWorkOrderCompletedRequest
import com.pos10.view.CoordinateManager
import com.pos10.view.MainActivity
import com.pos10.view.ObjectForTab
import com.pos10.view.SelectedRequestHolder
import com.pos10.view.SingletonObject
import com.pos10.view.navigation.Screen
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun JobsScreen(navHostController: NavHostController, viewModel: AuthViewModel = hiltViewModel()) {
    val scroll = rememberScrollState()
    var search by remember { mutableStateOf("") }
    var selectedIndex by remember { mutableStateOf(0) }
    val context = LocalContext.current
    (context as MainActivity).visibleStatusBar(context)
    val lifecycleOwner = LocalLifecycleOwner.current
    var showDialog by remember { mutableStateOf(false) }
    var showDialogAppointment by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()
    var selectedDatePicker by remember { mutableStateOf("DD/MM/YYYY") }
    var selectedFromTime by remember { mutableStateOf("HH:MM:SS") }
    val name = SharedPreference.get(context).name
    val initial = name.firstOrNull()?.uppercase() ?: "?"
    val activity = context as MainActivity
    var showSearch by remember { mutableStateOf(false) }
    var isDrawerOpen by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(ObjectForTab.tabRequestName) }
    val tabTitles = listOf("New", "In Progress", "Completed", "Failed", "Delayed")
    val selectedTabIndex = remember { mutableStateOf(ObjectForTab.tabName) }
    var location1 by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var loading by remember { mutableStateOf(true) }
    var closedRequest by remember { mutableStateOf(false) }
    (context as MainActivity).statusBar(context)

    LaunchedEffect(Unit) {
        val dbHelper = DatabaseHelper(context)
        while (true) {
            dbHelper.selectPositionAsync(object : DatabaseHelper.DatabaseHandler<Position?> {
                override fun onComplete(success: Boolean, result: Position?) {
                    if (success && result != null) {
                        location1 = Pair(result.latitude, result.longitude)
                        SharedPreference.get(context).latitude = result.latitude.toString()
                        SharedPreference.get(context).longitude = result.longitude.toString()
                    } else {
                        Log.w("TAG", "Location not found or failed.")
                    }
                    loading = false
                }
            })

            delay(60_000)
        }
    }


    if (showDialog) {
        CustomDialogBoxLogout(context, viewModel, lifecycleOwner, navHostController) { result ->
            showDialog = false
            when (result) {
                "yes" -> {
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
                    // No action needed, just dismiss
                }

                else -> {
                }
            }
        }
    }

    if (showDialogAppointment) {
        ScheduleAppointment(viewModel) {
            if (viewModel.rescheduleRefresh == true) {
                (context as? MainActivity)?.setupSync()
                viewModel.rescheduleRefresh = false
            }
            showDialogAppointment = false
            viewModel.showDatePicker = false
            viewModel.showTimePicker = false
        }
    }

    LaunchedEffect(Unit) {
        viewModel.updateWorkOrderCompleted.collect { status ->
            when (status) {
                CommonUtils.STATUS.INPROGRESS.sttausname -> {
                    selectedTabIndex.value = 1
                    ObjectForTab.tabName = 1
                    getWorkList(
                        selectedOption,
                        selectedWorkStatus(selectedTabIndex.value),
                        viewModel
                    )

                }

                CommonUtils.STATUS.FAILED.sttausname -> {

                }
            }
        }
    }


    // offline feature
    val allWorkOrders by viewModel.allWorkOrders.collectAsStateWithLifecycle()
    val workOrders by viewModel.filteredWorkOrders.collectAsStateWithLifecycle()

    val metaData by viewModel.workMetaData.collectAsStateWithLifecycle()
    val counts by viewModel.workOrderStatusCounts.collectAsStateWithLifecycle()

    val json = remember(workOrders) {
        Gson().toJson(workOrders)
    }

    Log.d("TAG", "JobsScreen: WorklistJSON---$json")

    //this logic i again wrote because for multiple request it wont working
    val closedRequestStatus by remember { mutableStateOf(mutableMapOf<Int, Boolean>()) }

    LaunchedEffect(workOrders, allWorkOrders) {
        val updatedMap = mutableMapOf<Int, Boolean>()

        workOrders.forEach { filteredWo ->
            val originalWo = allWorkOrders.find { it.workid == filteredWo.workid }
            val isClosed = originalWo?.let { viewModel.isWorkRequestClosed(it) } ?: false
            updatedMap[filteredWo.workid] = isClosed
        }

        closedRequestStatus.clear()
        closedRequestStatus.putAll(updatedMap)
    }

    viewModel.saveDataRequest = metaData
    viewModel.requestListChecklistData = metaData?.checklist ?: arrayListOf()

    LaunchedEffect(key1 = Unit) {
        getWorkList(selectedOption, selectedWorkStatus(selectedTabIndex.value), viewModel)
        observerList(
            context as MainActivity,
            viewModel,
            navHostController,
            lifecycleOwner,
            selectedOption,
            selectedTabIndex
        )
    }

    val drawerWidth = 280.dp
    val offsetX by animateDpAsState(
        targetValue = if (isDrawerOpen) 0.dp else -drawerWidth,
        animationSpec = tween(durationMillis = 300),
        label = "drawerOffset"
    )

    val assignedWorkOrders = workOrders.filter { wo ->
        val matchesSearch =
            wo.merchantName.contains(search, ignoreCase = true) ||
                    wo.workOrderNo.contains(search, ignoreCase = true)

        val isValidStatus =
            wo.workStatus.equals(CommonUtils.STATUS.ASSIGNED.sttausname, ignoreCase = true) ||
                    wo.workStatus.equals(
                        CommonUtils.STATUS.ITEMPACKED.sttausname,
                        ignoreCase = true
                    )

        matchesSearch && isValidStatus
    }

    // Split into two lists based on SLA breach
    val now = System.currentTimeMillis()
    val breachedList = assignedWorkOrders.filter { wo ->
        val dueDateMillis = parseDateToMillis(wo.dueDate)
        dueDateMillis != null && dueDateMillis < now
    }

    val nonBreachedList = assignedWorkOrders.filter { wo ->
        val dueDateMillis = parseDateToMillis(wo.dueDate)
        dueDateMillis == null || dueDateMillis >= now
    }

    val filteredList = if (selectedTabIndex.value == 0) {
        nonBreachedList
    } else if (selectedTabIndex.value == 4) {
        breachedList
    } else {
        workOrders.filter { item ->
            item.merchantName.contains(search, ignoreCase = true) ||
                    item.workOrderNo.contains(search, ignoreCase = true)
        }
    }.sortedWith(
        compareByDescending {
            it.workStatus.equals(
                CommonUtils.STATUS.INPROGRESS.sttausname,
                ignoreCase = true
            )
        }
    )



    Log.d("JobsScreen", "BreachedList---${filteredList}")

    val duedatelist = SelectedRequestHolder.selectedItemList
    val dueDate = duedatelist?.dueDate
    if (viewModel.showDatePicker) {
        showDatePickerDialog(
            context = context,
            dueDateString = dueDate ?: "08-09-2025 00:00:00",
            preSelectedDateString = viewModel.selectDatePicker.value, // Pass previously selected date
            onDateSelected = { selectedDate ->
                viewModel.showDatePicker = false
                viewModel.showTimePicker = false
                viewModel.selectDatePicker.value = selectedDate
                selectedDatePicker = selectedDate
                viewModel.selectedDateLong.value =
                    CommonUtils.convertDateToLong(selectedDate).toString()
            },
            onDismiss = {
                viewModel.showDatePicker = false
                viewModel.showTimePicker = false
            })
    }
    if (viewModel.showTimePicker) {
        showTimePickerDialog(
            context = context,
            selectedDate = viewModel.selectDatePicker.value,
            dueDateString = dueDate ?: "09-09-2025 11:00:00",
            preSelectedTime = viewModel.selectTimePicker.value,
            onTimeSelected = { time ->
                viewModel.showTimePicker = false
                viewModel.showDatePicker = false
                val timeFormatted = CommonUtils.convertTimestampToDateNew(time)
                viewModel.selectTimePicker.value = timeFormatted
                selectedFromTime = timeFormatted
                viewModel.selectedTimeLong.value = time.toString()
            },
            onDismiss = {
                viewModel.showTimePicker = false
            })
    }

    SingletonObject.savedBitmap.value = null
    SingletonObject.savedBitmapQR.value = null
    SingletonObject.uploadImageUriBusiness1.value = null

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (showSearch) {
                //  SEARCH BAR ROW
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Color.White)
                        .padding(top = 50.dp, bottom = 10.dp)
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = search,
                        onValueChange = { search = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontFamily = FontFamily(Font(R.font.instrument_sans_regular))
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = Color(0xFFEAEEF0),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(Color.White, shape = RoundedCornerShape(8.dp)),
                        decorationBox = { innerTextField ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp)
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.ic_search_ic),
                                    contentDescription = "Search",
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (search.isEmpty()) {
                                        Text(
                                            text = "Search merchants...",
                                            color = Color(0xFF35494F),
                                            fontSize = 14.sp,
                                            fontFamily = FontFamily(Font(R.font.instrument_sans_regular))
                                        )
                                    }
                                    innerTextField()
                                }

                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    painter = painterResource(R.drawable.close_ic),
                                    contentDescription = "Clear search",
                                    tint = Color.Gray,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable {
                                            search = ""
                                            showSearch = false
                                        })
                            }
                        })
                }
            } else {
                //  DEFAULT TOP BAR
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(top = 36.dp, bottom = 12.dp, start = 20.dp, end = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0xFFF6F6F6), shape = CircleShape)
                            .clickable {
                                isDrawerOpen = true
                            }, contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Image(
                        painter = painterResource(R.drawable.logo_pos10),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(width = 80.dp, height = 40.dp)
                            .padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    //  Search Icon (click to expand)
                    Image(
                        painter = painterResource(R.drawable.ic_search_ic),
                        contentDescription = "Search",
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }) {
                                showSearch = true
                            })

                    Spacer(Modifier.width(8.dp))

                    Image(
                        painter = painterResource(R.drawable.ic_notifications),
                        contentDescription = "Notifications",
                        modifier = Modifier
                            .size(16.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }) {
                            })
                }
            }
            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scroll)
                    .background(color = Color.White)
            ) {

                Spacer(Modifier.height(10.dp))

                Card(
                    shape = RoundedCornerShape(6.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        DashboardCard(
                            title = "Installation",
                            count = viewModel.saveDataRequest?.installation ?: 0,
                            iconRes = R.drawable.install_third,
                            isSelected = selectedOption == CommonUtils.REQUESTTYPE.INSTALLATION.title, //  Highlight logic
                            modifier = Modifier.weight(1f)
                        ) {
                            selectedOption =
                                CommonUtils.REQUESTTYPE.INSTALLATION.title //  Set on click
                            ObjectForTab.tabRequestName = CommonUtils.REQUESTTYPE.INSTALLATION.title
                            viewModel.selectedOptionModel.value =
                                CommonUtils.REQUESTTYPE.INSTALLATION.title
                            getWorkList(
                                selectedOption,
                                selectedWorkStatus(selectedTabIndex.value),
                                viewModel
                            )
                        }

                        /*DashboardCard(
                            title = "Complaint",
                            count = viewModel.saveDataRequest?.complains ?: 0,
                            iconRes = R.drawable.complaint_second,
                            isSelected = selectedOption == CommonUtils.REQUESTTYPE.COMPLAINT.title,
                            modifier = Modifier.weight(1f)
                        ) {
                            selectedOption =
                                CommonUtils.REQUESTTYPE.COMPLAINT.title //  Set on click
                            ObjectForTab.tabRequestName = CommonUtils.REQUESTTYPE.COMPLAINT.title
                            viewModel.selectedOptionModel.value =
                                CommonUtils.REQUESTTYPE.COMPLAINT.title
                            getWorkList(
                                selectedOption,
                                selectedWorkStatus(selectedTabIndex.value),
                                viewModel
                            )

                        }*/

                        DashboardCard(
                            title = "Return",
                            count = viewModel.saveDataRequest?.returnValue ?: 0,
                            iconRes = R.drawable.complaint_second,
                            isSelected = selectedOption == CommonUtils.REQUESTTYPE.RETURN.title,
                            modifier = Modifier.weight(1f)
                        ) {
                            selectedOption =
                                CommonUtils.REQUESTTYPE.RETURN.title //  Set on click
                            ObjectForTab.tabRequestName = CommonUtils.REQUESTTYPE.RETURN.title
                            viewModel.selectedOptionModel.value =
                                CommonUtils.REQUESTTYPE.RETURN.title
                            getWorkList(
                                selectedOption,
                                selectedWorkStatus(selectedTabIndex.value),
                                viewModel
                            )

                        }

                        /*DashboardCard(
                            title = "Paper Roll",
                            count = viewModel.saveDataRequest?.paperRoll ?: 0,
                            iconRes = R.drawable.paperroll_second,
                            isSelected = selectedOption == CommonUtils.REQUESTTYPE.PAPERROLL.title, //  Highlight logic
                            modifier = Modifier.weight(1f)
                        ) {
                            selectedOption =
                                CommonUtils.REQUESTTYPE.PAPERROLL.title//  Set on click
                            ObjectForTab.tabRequestName = CommonUtils.REQUESTTYPE.PAPERROLL.title

                            viewModel.selectedOptionModel.value =
                                CommonUtils.REQUESTTYPE.PAPERROLL.title
                            getWorkList(
                                selectedOption,
                                selectedWorkStatus(selectedTabIndex.value),
                                viewModel
                            )

                        }*/

                        DashboardCard(
                            title = "Replacement",
                            count = viewModel.saveDataRequest?.replacement ?: 0,
                            iconRes = R.drawable.paperroll_second,
                            isSelected = selectedOption == CommonUtils.REQUESTTYPE.REPLACEMENT.title, //  Highlight logic
                            modifier = Modifier.weight(1f)
                        ) {
                            selectedOption =
                                CommonUtils.REQUESTTYPE.REPLACEMENT.title//  Set on click
                            ObjectForTab.tabRequestName = CommonUtils.REQUESTTYPE.REPLACEMENT.title

                            viewModel.selectedOptionModel.value =
                                CommonUtils.REQUESTTYPE.REPLACEMENT.title
                            getWorkList(
                                selectedOption,
                                selectedWorkStatus(selectedTabIndex.value),
                                viewModel
                            )

                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex.value,
                    edgePadding = 16.dp,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex.value]),
                            color = Color(0xFFff6900)
                        )
                    }) {
                    LaunchedEffect(selectedTabIndex.value) {
                        if (tabTitles[selectedTabIndex.value] == "New") {
                            (context as? MainActivity)?.setupSync()
                        }
                    }
                    tabTitles.forEachIndexed { index, title ->
                        val isSelected = index == selectedTabIndex.value
                        val count = counts[title.lowercase()] ?: 0
                        Tab(
                            selected = isSelected,
                            onClick = {
                                Log.d(
                                    "TAG",
                                    "JobsScreen: DatatTab---${selectedTabIndex.value} ---$index"
                                )
                                selectedTabIndex.value = index
                                ObjectForTab.tabName = index
                                getWorkList(
                                    selectedOption,
                                    selectedWorkStatus(selectedTabIndex.value),
                                    viewModel
                                )
                            },
                            selectedContentColor = Color(0xFFff6900),
                            unselectedContentColor = Color.Gray,
                            text = {
                                Text(text = "$title ($count)", fontSize = 14.sp)
                            })
                    }
                }

                if (filteredList.isNotEmpty()) {
                    //  val expandedMap = remember { mutableStateMapOf<Int, Boolean>() }
                    val expandedMap = remember { mutableStateMapOf<String, Boolean>() }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 30.dp)
                            .heightIn(max = 500.dp)
                    ) {
                        items(filteredList.size) { index ->
                            val item = filteredList[index]
                            val isExpanded = expandedMap[item.workOrderNo] ?: false

                            //   val isExpanded = expandedMap[index] ?: false

                            val itemChecklist = viewModel.requestListChecklistData
                            viewModel.requestsubworkListData = item.woRequest

                            Spacer(modifier = Modifier.height(8.dp))

                            Card(
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }) {
                                        if (item.workStatus.toString()
                                                .contains(CommonUtils.STATUS.COMPLETED.sttausname)
                                        ) {
                                            SelectedRequestHolder.selectedItemList = item
                                            navHostController.navigate(Screen.FullWorkOrderDetail.route)
                                        }
                                    }) {

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(IntrinsicSize.Min)
                                ) {
                                    // Left colored status bar
                                    val requestStatuses =
                                        item.woRequest.map { it.status.lowercase() }
                                    val merchantName =
                                        item.woRequest.firstOrNull()?.merchantName ?: "N/A"
                                    val reqstatus = item.woRequest.firstOrNull()?.status ?: "N/A"

                                    val now = System.currentTimeMillis()

                                    val workStatusColor = when {
                                        requestStatuses.all {
                                            it.equals(
                                                "completed",
                                                ignoreCase = true
                                            )
                                        } -> Color(0xFF4CAF50)

                                        requestStatuses.any {
                                            it.equals(
                                                "failed",
                                                ignoreCase = true
                                            )
                                        } -> Color.Red

                                        requestStatuses.any {
                                            it.equals(
                                                "progress",
                                                ignoreCase = true
                                            ) || it.equals(
                                                "inprogress",
                                                ignoreCase = true
                                            )
                                        } -> Color(0xFF2196F3)

                                        // Assigned and not breached (due today or in the future)
                                        requestStatuses.any {
                                            it.equals("assigned", ignoreCase = true) ||
                                                    it.equals("itempacked", ignoreCase = true)
                                        } &&
                                                item.workStatus.equals(
                                                    CommonUtils.STATUS.ASSIGNED.sttausname,
                                                    ignoreCase = true
                                                ) ||
                                                item.workStatus.equals(
                                                    CommonUtils.STATUS.ITEMPACKED.sttausname,
                                                    ignoreCase = true
                                                ) &&
                                                (parseDateToMillis(item.dueDate)?.let { it >= now }
                                                    ?: true) -> Color.Black


                                        requestStatuses.any {
                                            it.equals("assigned", ignoreCase = true) ||
                                                    it.equals("itempacked", ignoreCase = true)
                                        } &&
                                                item.workStatus.equals(
                                                    CommonUtils.STATUS.ASSIGNED.sttausname,
                                                    ignoreCase = true
                                                ) ||
                                                item.workStatus.equals(
                                                    CommonUtils.STATUS.ITEMPACKED.sttausname,
                                                    ignoreCase = true
                                                ) &&
                                                (parseDateToMillis(item.dueDate)?.let { it < now }
                                                    ?: false) -> Color.Gray

                                        else -> Color.Gray
                                    }

                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .fillMaxHeight()
                                            .background(workStatusColor)
                                    )

                                    // Main content
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = when {
                                                        ObjectForTab.tabRequestName.contains("Installation") -> "Installation"
                                                        // ObjectForTab.tabRequestName.contains("Complaint") -> "Complaint"
                                                        // ObjectForTab.tabRequestName.contains("PaperRoll Delivery") -> "Delivery"
                                                        ObjectForTab.tabRequestName.contains("Return") -> "Return"
                                                        ObjectForTab.tabRequestName.contains("Replacement") -> "Replacement"
                                                        else -> "Installation"
                                                    },
                                                    fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)),
                                                    fontSize = 14.sp,
                                                    color = Color.Black
                                                )

                                                Spacer(modifier = Modifier.width(4.dp))
                                            }

                                            Spacer(modifier = Modifier.weight(1f))

                                            Spacer(modifier = Modifier.padding(start = 4.dp))

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .background(
                                                        color = Color.Gray,
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 0.dp)
                                            ) {
                                                Text(
                                                    text = "${item.woRequest.size}",
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
                                                    lineHeight = 12.sp
                                                )

                                                Text(
                                                    text = "W.Req",
                                                    fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
                                                    fontSize = 12.sp,
                                                    modifier = Modifier.padding(start = 4.dp),
                                                    color = Color.White
                                                )
                                            }

                                            Spacer(Modifier.width(3.dp))
                                            closedRequest = closedRequestStatus[item.workid] == true
                                            if (reqstatus.contains(CommonUtils.STATUS.COMPLETED.sttausname) || reqstatus.contains(
                                                    CommonUtils.STATUS.FAILED.sttausname
                                                )
                                            ) {
                                                if (item.workStatus.contains(CommonUtils.STATUS.INPROGRESS.sttausname)) {
                                                    if (closedRequest == true) {
                                                        Text(
                                                            text = "W.Complete",
                                                            fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)),
                                                            fontSize = 12.sp,
                                                            textDecoration = TextDecoration.Underline,
                                                            color = Color(0xFFff6900),
                                                            modifier = Modifier.clickable {
                                                                SelectedRequestHolder.selectedItemList =
                                                                    item
                                                                navHostController.navigate(Screen.JobUploadScreen.route)
                                                            })
                                                    }
                                                }
                                            }
                                        }


                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Start
                                        ) {

                                            Image(
                                                painter = painterResource(R.drawable.ic_person),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .padding(top = 8.dp, end = 8.dp)
                                            )

                                            Text(
                                                text = "$merchantName (${item.workOrderNo ?: "N/A"})",
                                                fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)),
                                                fontSize = 12.sp,
                                                modifier = Modifier
                                                    .padding(top = 4.dp)
                                                    .width(150.dp),
                                                maxLines = 2,
                                                color = Color.Black
                                            )

                                            Spacer(Modifier.weight(1f))
                                            val level =
                                                CommonUtils.SLA.fromApiValueSla(item.breachMessage)

                                            Text(
                                                text = level.sla,
                                                fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)),
                                                fontSize = 12.sp,
                                                color = level.color
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Start,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Image(
                                                painter = painterResource(R.drawable.ic_email),
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = item.email ?: "N/A",
                                                fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
                                                fontSize = 12.sp,
                                                color = Color.Gray,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(Modifier.width(8.dp))

                                            if (reqstatus.contains(CommonUtils.STATUS.ASSIGNED.sttausname) || reqstatus.contains(
                                                    CommonUtils.STATUS.INPROGRESS.sttausname
                                                )
                                            ) {
                                                val level =
                                                    CommonUtils.Priority.fromApiValue(item.priority?.toInt())
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 8.dp)
                                                ) {
                                                    Text(
                                                        text = level.label,
                                                        fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)),
                                                        fontSize = 12.sp,
                                                        color = level.color
                                                    )
                                                }
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Start,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Image(
                                                painter = painterResource(R.drawable.ic_phone),
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))

                                            Text(
                                                text = item.mobile ?: "00000000",
                                                fontSize = 12.sp,
                                                textDecoration = TextDecoration.Underline,
                                                fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
                                                color = Color.Gray,
                                                modifier = Modifier.clickable(
                                                    indication = null,
                                                    interactionSource = remember { MutableInteractionSource() }) {
                                                    val phoneNumber = item.mobile
                                                    CommonUtils.dialPhoneNumber(
                                                        context,
                                                        phoneNumber
                                                    )
                                                })

                                            Spacer((Modifier.weight(1f)))
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        try {
                                                            val uri =
                                                                Uri.parse(
                                                                    "https://www.google.com/maps/dir/?api=1&destination=${
                                                                        Uri.encode(item.location ?: "sector 66,noida")
                                                                    }"
                                                                )
                                                            val mapIntent =
                                                                Intent(
                                                                    Intent.ACTION_VIEW,
                                                                    uri
                                                                ).apply {
                                                                    setPackage("com.google.android.apps.maps")
                                                                }
                                                            context.startActivity(mapIntent)
                                                        } catch (e: Exception) {
                                                            Toast.makeText(
                                                                context,
                                                                "Unable to open Google Maps",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }, horizontalAlignment = Alignment.End
                                            ) {
                                                val latString =
                                                    SharedPreference.get(context).latitude
                                                val lngString =
                                                    SharedPreference.get(context).longitude

                                                val currentLat =
                                                    latString?.toDoubleOrNull() ?: 25.1873
                                                val currentLng =
                                                    lngString?.toDoubleOrNull() ?: 55.2666

                                                Log.d(
                                                    "TAG",
                                                    "JobsScreen: Lll----$currentLng----$currentLat"
                                                )

                                                LaunchedEffect(item.workOrderNo) {
                                                    viewModel.calculateDistanceFromCurrentToDestination(
                                                        context,
                                                        currentLat,
                                                        currentLng,
                                                        item.location,
                                                        item.workOrderNo,
                                                        item.latLong
                                                    )
                                                }

                                                val distance =
                                                    viewModel.distanceMap[item.workOrderNo]
                                                        ?: "Calculating..."

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Image(
                                                        painter = painterResource(R.drawable.location_pin_gradient),
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp)
                                                    )

                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = distance,
                                                        fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)),
                                                        fontSize = 12.sp,
                                                        color = Color.Black
                                                    )

                                                    Image(
                                                        painter = painterResource(id = if (!isExpanded) R.drawable.ic_down else R.drawable.ic_up),
                                                        contentDescription = "Arrow",
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .clickable {
                                                                expandedMap[item.workOrderNo] =
                                                                    !(expandedMap[item.workOrderNo]
                                                                        ?: false)
                                                            })
                                                }
                                            }

                                        }

                                        if (isExpanded) {
                                            Column {
                                                Spacer(modifier = Modifier.width(4.dp))

                                                Row(
                                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Start,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Image(
                                                        painter = painterResource(R.drawable.location_pin_grey),
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Text(
                                                        text = item.location ?: "N/A",
                                                        fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
                                                        fontSize = 12.sp,
                                                        color = Color.Gray
                                                    )
                                                }

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {

                                                    if (reqstatus.contains(CommonUtils.STATUS.COMPLETED.sttausname) || reqstatus.contains(
                                                            CommonUtils.STATUS.FAILED.sttausname
                                                        )
                                                    ) {
                                                    } else {
                                                        if (item.appointmentDate != "" && item.appointmentTime != "") {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.Start,
                                                                modifier = Modifier.fillMaxWidth()
                                                            ) {
                                                                Image(
                                                                    painter = painterResource(R.drawable.ic_calendar),
                                                                    contentDescription = null,
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                                Spacer(Modifier.width(4.dp))
                                                                val formattedDate =
                                                                    if (item.appointmentDate.contains(
                                                                            "00:00:00"
                                                                        )
                                                                    ) {
                                                                        item.appointmentDate.replace(
                                                                            "00:00:00",
                                                                            ""
                                                                        ).trim()
                                                                    } else {
                                                                        item.appointmentDate
                                                                    }

                                                                Text(
                                                                    text = "$formattedDate, ${item.appointmentTime}",
                                                                    color = Color.Gray,
                                                                    fontSize = 12.sp,
                                                                    fontFamily = FontFamily(Font(R.font.instrument_sans_regular))
                                                                )

                                                                if (item.appointmentDate != "" && item.appointmentTime != "") {
                                                                    Spacer(Modifier.weight(1f))
                                                                    Log.d(
                                                                        "TAG",
                                                                        "JobsScreen: Request ------${item.workStatus}"
                                                                    )
                                                                    if (item.workStatus.contains(
                                                                            CommonUtils.STATUS.ASSIGNED.sttausname
                                                                        ) || item.workStatus.contains(
                                                                            CommonUtils.STATUS.ITEMPACKED.sttausname
                                                                        )
                                                                    ) {
                                                                        Text(
                                                                            text = "Job Start",
                                                                            color = Color(0xFFff6900),
                                                                            fontSize = 12.sp,
                                                                            modifier = Modifier.clickable {
                                                                                Log.d(
                                                                                    "TAG",
                                                                                    "JobsScreen:DateFormat---${item.appointmentDate} "
                                                                                )
                                                                                SelectedRequestHolder.selectedItemList =
                                                                                    item
                                                                                val appointment =
                                                                                    item.appointmentDate

                                                                                val model =
                                                                                    UpdateWorkOrderCompletedRequest(
                                                                                        item?.workid.toString(),
                                                                                        CommonUtils.STATUS.INPROGRESS.type,
                                                                                        SharedPreference.get(
                                                                                            context
                                                                                        ).userId.toInt(),
                                                                                        "",
                                                                                        "",
                                                                                        ""
                                                                                    )

                                                                                viewModel.hitUpdateWorkOrderCompleted(
                                                                                    returnAccessToken(
                                                                                        context
                                                                                    ),
                                                                                    model,
                                                                                    CommonUtils.STATUS.INPROGRESS.sttausname
                                                                                )
                                                                            },
                                                                            textDecoration = TextDecoration.Underline,
                                                                            fontFamily = FontFamily(
                                                                                Font(R.font.instrument_sans_regular)
                                                                            )
                                                                        )
                                                                    }
                                                                }

                                                            }
                                                        } else {
                                                        }
                                                    }
                                                }
                                            }

                                            item.woRequest.forEach { itemW ->
                                                Card(
                                                    elevation = CardDefaults.cardElevation(
                                                        defaultElevation = 4.dp
                                                    ),
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = Color(
                                                            0xFFf9fafb
                                                        )
                                                    ),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 8.dp)
                                                ) {
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(
                                                                horizontal = 12.dp,
                                                                vertical = 8.dp
                                                            )
                                                            .clickable {
                                                                if (itemW.status.contains("Failed")) {
                                                                    showToastC(
                                                                        context,
                                                                        "This request is failed, please contact admin."
                                                                    )
                                                                } else if (itemW.status.contains("Completed")) {
                                                                    SelectedRequestHolder.selectedItemList =
                                                                        item
                                                                    SelectedRequestHolder.selectedSUbWorkItemList =
                                                                        itemW
                                                                    SelectedRequestHolder.selectedItemCheckList =
                                                                        itemChecklist
                                                                    when {
                                                                        /*itemW.requesttype.contains("Complaint") -> {
                                                                            navHostController.navigate(
                                                                                Screen.ComplaintScreen.route
                                                                            )
                                                                        }

                                                                        itemW.requesttype.contains("PaperRoll") -> {
                                                                            navHostController.navigate(
                                                                                Screen.PaperRollScreen.route
                                                                            )
                                                                        }*/

                                                                        itemW.requesttype.contains("Return") -> {
                                                                            navHostController.navigate(
                                                                                Screen.ReturnDeviceDetails.route
                                                                            )
                                                                        }

                                                                        itemW.requesttype.contains("Replacement") -> {
                                                                            navHostController.navigate(
                                                                                Screen.ReplacementDetailScreen.route
                                                                            )
                                                                        }

                                                                        else -> {
                                                                            navHostController.navigate(
                                                                                Screen.JobInstallationDetailScreen.route
                                                                            )
                                                                        }
                                                                    }
                                                                } else {
                                                                    if (item.appointmentDate.equals(
                                                                            ""
                                                                        ) && item.appointmentTime.equals(
                                                                            ""
                                                                        )
                                                                    ) {
                                                                        showToastC(
                                                                            context,
                                                                            "Please book the appointment on the portal."
                                                                        )
                                                                        return@clickable
                                                                    }

                                                                    if (itemW.status.contains(
                                                                            CommonUtils.STATUS.ASSIGNED.sttausname
                                                                        )
                                                                    ) {
                                                                        showToastC(
                                                                            context,
                                                                            "Please start the job"
                                                                        )
                                                                        return@clickable
                                                                    }

                                                                    SelectedRequestHolder.selectedItemList =
                                                                        item
                                                                    SelectedRequestHolder.selectedItemCheckList =
                                                                        itemChecklist
                                                                    SelectedRequestHolder.selectedSUbWorkItemList =
                                                                        itemW

                                                                    when {
                                                                        /*itemW.requesttype.contains("Complaint") -> {
                                                                            navHostController.navigate(
                                                                                Screen.ComplaintScreen.route
                                                                            )
                                                                        }

                                                                        itemW.requesttype.contains("PaperRoll") -> {
                                                                            navHostController.navigate(
                                                                                Screen.PaperRollScreen.route
                                                                            )
                                                                        }*/

                                                                        itemW.requesttype.contains("Return") -> {
                                                                            navHostController.navigate(
                                                                                Screen.ReturnDeviceDetails.route
                                                                            )
                                                                        }

                                                                        itemW.requesttype.contains("Replacement") -> {
                                                                            navHostController.navigate(
                                                                                Screen.ReplacementDetailScreen.route
                                                                            )
                                                                        }

                                                                        else -> {
                                                                            navHostController.navigate(
                                                                                Screen.JobInstallationDetailScreen.route
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            }) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                        ) {
                                                            Text(
                                                                text = "Req.No: ${itemW.requestNo}",
                                                                fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)),
                                                                fontSize = 12.sp,
                                                                color = Color.Black
                                                            )
                                                        }

                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                            Text(
                                                                text = "Device: ",
                                                                fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)),
                                                                fontSize = 12.sp,
                                                                color = Color.Black
                                                            )
                                                            var deviceTypeValue = ""
                                                            if (itemW.deviceType.equals("")) {
                                                                deviceTypeValue = "--"
                                                            } else {
                                                                deviceTypeValue = itemW.deviceType
                                                            }
                                                            Text(
                                                                text = deviceTypeValue,
                                                                fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)),
                                                                fontSize = 12.sp,
                                                                color = Color.Black
                                                            )
                                                        }

                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(
                                                                text = "Status: ",
                                                                fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)),
                                                                fontSize = 12.sp,
                                                                color = Color.Black
                                                            )

                                                            Text(
                                                                text = "${itemW.status}",
                                                                fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)),
                                                                fontSize = 12.sp,
                                                                color = when (itemW.status.lowercase()) {
                                                                    "completed" -> Color(0xFF4CAF50)
                                                                    "assigned" -> Color.Black
                                                                    "progress", "inprogress" -> Color(
                                                                        0xFF2196F3
                                                                    )

                                                                    else -> Color.Gray
                                                                }
                                                            )
                                                        }
                                                    }

                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "No data found",
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)),
                            color = Color.Black
                        )
                    }
                }
            }
        }

        if (isDrawerOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { isDrawerOpen = false }
                    .zIndex(1f))
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(drawerWidth)
                .offset(x = offsetX)
                .background(Color.White)
                .zIndex(2f)
        ) {
            SideProfileScreen(navHostController)
        }
    }
}

fun observerList(
    context: MainActivity,
    viewModel: AuthViewModel,
    navController: NavHostController?,
    lifecycleOwner: LifecycleOwner,
    selectedOption: String,
    selectedTabIndex: MutableState<Int>,
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
}

fun getWorkList(requestType: String, workstatus: List<String>, viewModel: AuthViewModel) {
    viewModel.setRequestType(requestType)
    viewModel.setWorkStatus(workstatus)
}

fun selectedWorkStatus(selectedTabIndex: Int): List<String> {
    return when (selectedTabIndex) {
        0 -> listOf(
            CommonUtils.STATUS.ASSIGNED.sttausname,
            CommonUtils.STATUS.ITEMPACKED.sttausname
        )

        1 -> listOf(CommonUtils.STATUS.INPROGRESS.sttausname)
        2 -> listOf(CommonUtils.STATUS.COMPLETED.sttausname)
        3 -> listOf(CommonUtils.STATUS.FAILED.sttausname)
        4 -> listOf(CommonUtils.STATUS.ASSIGNED.sttausname)
        else -> listOf(CommonUtils.STATUS.CREATED.sttausname)
    }
}

/*
fun selectedWorkStatus(selectedTabIndex: Int): String {
    val type = when (selectedTabIndex) {
        0 -> {
            CommonUtils.STATUS.ASSIGNED.sttausname
            CommonUtils.STATUS.ITEMPACKED.sttausname
        }

        1 -> CommonUtils.STATUS.INPROGRESS.sttausname
        2 -> CommonUtils.STATUS.COMPLETED.sttausname
        3 -> CommonUtils.STATUS.FAILED.sttausname
        4 -> CommonUtils.STATUS.ASSIGNED.sttausname
        else -> CommonUtils.STATUS.CREATED.sttausname
    }
    return type
}*/





