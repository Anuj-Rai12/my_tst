package com.pos10.view.screens

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.pos10.R
import com.pos10.domain.AuthViewModel
import com.pos10.helper.CommonUtils
import com.pos10.helper.CommonUtils.convertDateTimeFormat
import com.pos10.helper.CommonUtils.convertDateTimeToDate
import com.pos10.helper.CommonUtils.isInvalid
import com.pos10.helper.CommonUtils.returnAccessToken
import com.pos10.helper.CommonUtils.showToastC
import com.pos10.helper.CustomLoader
import com.pos10.helper.EmpResource
import com.pos10.helper.ErrorUtil
import com.pos10.helper.NetworkUtils.isOnline
import com.pos10.helper.SharedPreference
import com.pos10.model.local.QRCodeScanerRequest
import com.pos10.model.local.UpdateRequest
import com.pos10.view.MainActivity
import com.pos10.view.SelectedRequestHolder
import com.pos10.view.SingletonObject
import com.pos10.view.navigation.Screen
import com.pos10.view.screens.qrscanner.QRScannerScreen
import com.pos10.view.screens.qrscanner.generateQRCodeBitmap

data class SelectableItem(
    val name: String,
    var id: Int,
    var type: Int,
    val isSelected: Boolean? = null
)

@Composable
fun InstallationDeviceDetails(
    navHostController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    (context as MainActivity).visibleStatusBar(context)
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        observerUpdate(context, viewModel, navHostController, lifecycleOwner)
    }

    val item = SelectedRequestHolder.selectedSUbWorkItemList
    val itemMain = SelectedRequestHolder.selectedItemList
    val itemChecklist = SelectedRequestHolder.selectedItemCheckList
    Log.d("TAG", "InstallationDeviceDetails Item: $item---$itemChecklist")

    //qr
    val workOrders by viewModel.workOrders.collectAsState()
    val cancelList by viewModel.allCancelList.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.updateSuccess.collect { status ->
            Log.d("TAG", "InstallationDeviceDetails: Status ---${status} ")
            when (status) {
                CommonUtils.STATUS.INPROGRESS.sttausname -> {
                    SelectedRequestHolder.selectedSUbWorkItemList?.status =
                        CommonUtils.STATUS.INPROGRESS.sttausname
                    navHostController.navigate(Screen.UploadDocumentsScreen.route)
                }

                CommonUtils.STATUS.FAILED.sttausname -> {
                    SelectedRequestHolder.selectedSUbWorkItemList?.status =
                        CommonUtils.STATUS.FAILED.sttausname
                    navHostController.navigate(Screen.DashboardScreen.route)
                }
            }
        }
    }

    val currentRequest =
        workOrders.flatMap { it.woRequest }.firstOrNull { it.requestid == item?.requestid }

    item?.installationStatus = currentRequest?.installationStatus.toString()
    item?.ischecklistDone = currentRequest?.ischecklistDone ?: 0

    Log.d(
        "TAG",
        "InstallationDeviceDetails InstallationStatus:1---${item?.requestid}--${currentRequest?.installationStatus.toString()}---------${item?.installationStatus}"
    )
    Log.d(
        "TAG",
        "InstallationDeviceDetails Checklist:1---${item?.requestid}--${currentRequest?.ischecklistDone.toString()}---------${item?.ischecklistDone}"
    )

    BackHandler {
        val currentRoute = navHostController.currentBackStackEntry?.destination?.route
        Log.d("BackHandler", "Current route: $currentRoute")

        if (currentRoute == Screen.JobInstallationDetailScreen.route) {
            Log.d("BackHandler", "Current route:true")
            navHostController.navigate(Screen.DashboardScreen.route) {
                popUpTo(0) {
                    inclusive = true
                }
            }
        } else {
            Log.d("BackHandler", "Current route:False")
            navHostController.popBackStack()
        }
    }

    val itemList = remember {
        mutableStateListOf<SelectableItem>().apply {
            itemChecklist?.filter { it.type.toInt() == 27702 }?.forEach { checklistItem ->
                add(
                    SelectableItem(
                        name = checklistItem.value,
                        id = checklistItem.id,
                        type = checklistItem.type.toInt(),
                        isSelected = checklistItem.isSelected
                    )
                )
            }
        }
    }

    val appointment = itemMain?.appointmentDate
    val finalDate =
        if (!appointment.isNullOrEmpty() && Regex("\\d{4}-\\d{2}-\\d{2}").matches(appointment)) {
            // Already in yyyy-MM-dd format, use as is
            appointment
        } else {
            // Not in yyyy-MM-dd format → convert
            val dattee = convertDateTimeToDate(appointment ?: "18-09-2025 00:00:00")
            convertDateTimeFormat(dattee)
        }
    var showSheet by remember { mutableStateOf(false) }
    CancelRequestBottomSheet(
        cancelList,
        showSheet = showSheet,
        onDismiss = { showSheet = false },
        onSubmit = { reasonId, remark ->
            showSheet = false
            Log.d("CancelWork", "ReasonId=$reasonId, Remark=$remark")

            // Send data to API
            SingletonObject.fromWhere = "installation"
            val model = UpdateRequest(
                "",
                "1900-01-01",
                false,
                "",
                SharedPreference.get(context).userId.toInt(),
                0,
                0,
                item?.requestid?.toInt() ?: 0,
                CommonUtils.STATUS.FAILED.type,
                0,
                finalDate,
                itemMain?.appointmentTime ?: "04:06 pm",
                reasonId,
                remark
            )
            viewModel.hitUpdateRequest(
                model,
                CommonUtils.STATUS.FAILED.sttausname
            )
        })

    LaunchedEffect(Unit) {
        viewModel.setRequestId(item?.requestid.toString())
        viewModel.setRequestTypeee("R")
        Log.d("TAG", "InstallationDeviceDetails:Offline ---${item?.requestid.toString()} ")
    }

    val downloadFiles by viewModel.allFiles.collectAsState()
    val downloadList = downloadFiles.filter { item ->
        item.filePath.contains("", ignoreCase = true)
    }


    // Main container with vertical layout
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 20.dp)
                .background(color = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White)
                    .padding(top = 40.dp, bottom = 10.dp)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_back_black),
                    contentDescription = "",
                    modifier = Modifier
                        .width(16.dp)
                        .clickable {
                            val currentRoute =
                                navHostController.currentBackStackEntry?.destination?.route
                            Log.d("BackHandler", "Current route: $currentRoute")

                            if (currentRoute == Screen.JobInstallationDetailScreen.route) {
                                Log.d("BackHandler", "Current route:true")
                                navHostController.navigate(Screen.DashboardScreen.route) {
                                    popUpTo(0) {
                                        inclusive = true
                                    }
                                }

                            } else {
                                Log.d("BackHandler", "Current route:False")
                                navHostController.popBackStack()
                            }
                            // navHostController.popBackStack()
                        },
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Merchant Id:${item?.merchantCode}",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold))
                )
            }

            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {

                Log.d("TAG", "InstallationDeviceDetails:Status22 ----${item?.installationStatus} ")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Job Status",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold))
                    )

                    Log.d(
                        "TAG",
                        "InstallationDeviceDetails:StatusNogfff ----${item?.installationStatus} "
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = Color(0xFF4CAF50),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }) {
                                if (item?.installationStatus.equals("0")) {
                                    showToastC(context, "Please scan the QR code")
                                    viewModel.showDialogQr = true
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 2.dp)) {
//                                if(viewModel.updateQr==true){
//                                    Image(painter = painterResource(R.drawable.circleshape_checked),
//                                        contentDescription = null,
//                                        modifier = Modifier.size(12.dp))
//                                }else {
                        Image(
                            painter = painterResource(if (item?.installationStatus?.contains("0") == true) R.drawable.circleshape_unchecked else R.drawable.circleshape_checked),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        // }

                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (item?.installationStatus?.contains("0") == true) "Activate" else "Active",
                            fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
                            fontSize = 12.sp,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        var simNo = ""
                        var sno = ""
                        if (item?.simnumber.equals("")) {
                            simNo = viewModel.simno.value
                            sno = viewModel.serialnumber.value
                        } else {
                            simNo = item?.simnumber ?: "N/A"
                            sno = item?.serialNo ?: "N/A"
                        }

                        Log.d("TAG", "InstallationDeviceDetailsdada:${viewModel.simno.value} ")
                        InfoRow(label = "Serial No:", value = sno.ifBlank { "N/A" })
                        InfoRow(label = "Device Type:", value = item?.deviceType ?: "N/A")
                        InfoRow(label = "Merchant Name:", value = item?.merchantName ?: "N/A")
                        InfoRow(label = "Device ID:", value = item?.deviceNo ?: "N/A")
                        InfoRow(label = "Sim No:", value = simNo.ifBlank { "N/A" })
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    "Device Setup",
                    modifier = Modifier.padding(start = 16.dp),
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.instrument_sans_medium)),
                    color = Color.Black
                )

                Spacer(Modifier.height(4.dp))
                Text(
                    "Installation Checklist",
                    modifier = Modifier.padding(start = 16.dp),
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
                    color = Color.Black
                )
                Spacer(Modifier.height(8.dp))

                Column(modifier = Modifier.padding(start = 16.dp, top = 0.dp, end = 16.dp)) {
                    itemList.forEachIndexed { index, itemname ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }) {
                                    if (item?.status.equals(CommonUtils.STATUS.COMPLETED.sttausname)) {

                                    } else {
                                        itemname.isSelected?.let {
                                            itemList[index] =
                                                itemname.copy(isSelected = !it)
                                        }
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {

                            if (item?.status.equals(CommonUtils.STATUS.COMPLETED.sttausname) || item?.ischecklistDone == 1) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_checked),
                                    contentDescription = if (itemname.isSelected == true) "Checked" else "Unchecked",
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Image(
                                    painter = painterResource(
                                        id = if (itemname.isSelected == true) R.drawable.ic_checked
                                        else R.drawable.ic_uncheckedbox
                                    ),
                                    contentDescription = if (itemname.isSelected == true) "Checked" else "Unchecked",
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = itemname.name,
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
                                color = Color.Black
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                //add download file
                if (item?.status.equals(CommonUtils.STATUS.COMPLETED.sttausname)) {
                    if (!downloadList.isNullOrEmpty()) {
                        Text(
                            text = "Download Files",
                            fontSize = 16.sp,
                            color = Color.Black,
                            fontFamily = FontFamily(Font(R.font.instrument_sans_medium)),
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                        )

                        val downDistinctFiles =
                            downloadList.groupBy { it.originalFileName }.mapNotNull { (_, files) ->
                                // Pick the one that contains the full URL (http/https)
                                files.firstOrNull { it.filePath.startsWith("http") }
                            }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                                .padding(horizontal = 16.dp)
                        ) {
                            items(downDistinctFiles) { fileItem ->
                                val fileName = fileItem.originalFileName ?: "File"
                                val fileUrl = fileItem.filePath ?: ""
                                val uploadType = fileItem.uploadType ?: ""

                                Text(
                                    text = "$uploadType:",
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)),
                                    color = Color.Black,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .clickable {
                                            downloadFile(context, fileUrl, fileName)
                                        }) {
                                    // Thumbnail
                                    if (fileName.endsWith(".pdf", true) || fileName.endsWith(
                                            ".xls",
                                            true
                                        ) || fileName.endsWith(".xlsx", true)
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.pdf),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                        )
                                    } else {
                                        AsyncImage(
                                            model = fileUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(
                                                    1.dp,
                                                    Color(0xFFF6F6F6),
                                                    RoundedCornerShape(8.dp)
                                                )
                                        )

                                    }

                                    Spacer(modifier = Modifier.width(12.dp))
                                    // File name
                                    Text(
                                        text = fileName,
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
                                        color = Color.Black,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Image(
                                        painter = painterResource(R.drawable.ic_download),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .size(24.dp)
                                            .clickable {
                                                downloadFile(context, fileUrl, fileName)
                                            })
                                }

                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    if (!item?.status.equals(CommonUtils.STATUS.COMPLETED.sttausname)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFff6900),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(color = Color.White)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }) {
                                    Log.d("TAG", "InstallationDeviceDetails: gggg ${item?.status}")
                                    if (!item?.status.equals(CommonUtils.STATUS.COMPLETED.sttausname)) {/*  if (item?.status?.contains("Assigned") == true) {
                                    Toast.makeText(context,
                                        "Please start the installation first",
                                        Toast.LENGTH_SHORT).show()
                                    return@clickable
                                }*/

                                        if (item?.installationStatus?.contains("0") == true) {
                                            showToastC(
                                                context,
                                                "Please activate the installation process first"
                                            )
                                            return@clickable
                                        }

                                        val updatedItemList = itemList.map { itemindex ->
                                            if (item?.ischecklistDone == 1) itemindex.copy(
                                                isSelected = true
                                            )
                                            else itemindex
                                        }
                                        val notSelectedItems =
                                            updatedItemList.filterNot { it.isSelected == true }
//                                val notSelectedItems = itemList.filterNot { it.isSelected }
                                        //notSelectedItems.isNotEmpty()
                                        if (notSelectedItems.isNotEmpty()) {
                                            Toast.makeText(
                                                context,
                                                "Please verified the installation checklist",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            // All items are selected, collect list
                                            val selectedItemList = itemList.map { it.copy() }
                                            Log.d("TAG", "All Selected Items: $selectedItemList")
                                            SingletonObject.fromWhere = "installation"
                                            val model = UpdateRequest(
                                                "",
                                                "1900-01-01",
                                                true,
                                                "",
                                                SharedPreference.get(context).userId.toInt(),
                                                0,
                                                0,
                                                item?.requestid?.toInt() ?: 0,
                                                CommonUtils.STATUS.INPROGRESS.type,
                                                0,
                                                finalDate,
                                                itemMain?.appointmentTime ?: "04:06 pm"
                                            )
                                            viewModel.hitUpdateRequest(
                                                model,
                                                CommonUtils.STATUS.INPROGRESS.sttausname
                                            )
                                        }
                                    }
                                }, contentAlignment = Alignment.Center
                        ) {

                            Text(
                                text = "Setup Complete",
                                color = Color(0xFFff6900),
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }

                    if (!item?.status.equals(CommonUtils.STATUS.COMPLETED.sttausname)) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = 1.dp,
                                    color = Color.Gray,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(color = Color.White)
                                .clickable {
                                    showSheet = true
                                }, contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Setup Failed",
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        if (viewModel.showDialogQr) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f)
                    .background(Color.Black.copy(alpha = 0.85f))
            ) {
                QRScannerScreen(onQRCodeScanned = { qrCode ->
                    try {
                        val keyValuePairs = qrCode.split("&")
                        val dataMap = keyValuePairs.map { it.split("=") }.filter { it.size == 2 }
                            .associate { it[0] to it[1] }

                        Log.d("TAG", "InstallationDeviceDetails: Simmmm----$dataMap")
                        val simNumber = dataMap["SIMNO"] ?: ""
                        val serialNumber = dataMap["SNO"] ?: ""
                        val timestamp = dataMap["TIMESTAMP"] ?: ""

                        Log.d("TAG", "SIM Number: $simNumber")
                        Log.d("TAG", "Serial Number: $serialNumber")
                        Log.d("TAG", "Timestamp: $timestamp")

                        viewModel.simno.value = simNumber
                        viewModel.serialnumber.value = serialNumber
                        val item = SelectedRequestHolder.selectedSUbWorkItemList
                        SingletonObject.savedBitmapQR.value = generateQRCodeBitmap(qrCode)

                        if (serialNumber.isInvalid() || simNumber.isInvalid()) {
                            showToastC(context, "Please scan a valid QR code")
                            viewModel.showDialogQr = false
                            return@QRScannerScreen
                        }

                        Log.d(
                            "TAG",
                            "InstallationDeviceDetails:Scanned ${item?.deviceNo}--${item?.merchantCode} "
                        )

                        if (isOnline(context)) {
                            val qrmodel = QRCodeScanerRequest(
                                item?.deviceNo?.toInt() ?: 0,
                                item?.requestid?.toInt() ?: 0,
                                serialNumber,
                                simNumber,
                                SharedPreference.get(context).userId.toInt(),
                                item?.merchantCode ?: "",
                                timestamp
                            )
                            viewModel.hitQRCode(qrmodel)
                            viewModel.showDialogQr = false
                        } else {
                            showToastC(context, "Internet is not available")
                        }
                    } catch (e: Exception) {
                        Log.e("TAG", "Error parsing QR: ${e.message}")
                    }
                })
            }
        }

    }
}

fun observerUpdate(
    context: MainActivity,
    viewModel: AuthViewModel,
    navController: NavHostController?,
    lifecycleOwner: LifecycleOwner,
) {
    viewModel.updateLiveData.observe(lifecycleOwner) {
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
                viewModel.updatestatus = true
                if (it.value.data.status.equals(CommonUtils.STATUS.INPROGRESS.type) && it.value.data.isChecklistDone == false) {
                    SelectedRequestHolder.selectedSUbWorkItemList?.status =
                        CommonUtils.STATUS.INPROGRESS.sttausname
                } else if (it.value.data.status.equals(CommonUtils.STATUS.INPROGRESS.type) && it.value.data.isChecklistDone == true) {
                    SelectedRequestHolder.selectedSUbWorkItemList?.status =
                        CommonUtils.STATUS.INPROGRESS.sttausname
                    navController?.navigate(Screen.UploadDocumentsScreen.route)
                } else if (it.value.data.status.equals(CommonUtils.STATUS.COMPLETED.type)) {
                    SelectedRequestHolder.selectedSUbWorkItemList?.status =
                        CommonUtils.STATUS.COMPLETED.sttausname
                } else if (it.value.data.status.equals(CommonUtils.STATUS.FAILED.type)) {
                    SelectedRequestHolder.selectedSUbWorkItemList?.status =
                        CommonUtils.STATUS.FAILED.sttausname
                    navController?.navigate(Screen.DashboardScreen.route)
                } else if (it.value.data.status.equals(CommonUtils.STATUS.ITEMPACKED.type)) {
                    SelectedRequestHolder.selectedSUbWorkItemList?.status =
                        CommonUtils.STATUS.ITEMPACKED.sttausname
                } else {
                    SelectedRequestHolder.selectedSUbWorkItemList?.status =
                        CommonUtils.STATUS.ASSIGNED.sttausname
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
            color = Color.Black
        )

        Text(
            text = value,
            fontSize = 14.sp,
            fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
            color = Color.Gray
        )
    }
}

