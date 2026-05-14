package com.pos10.view.screens

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.pos10.R
import com.pos10.domain.AuthViewModel
import com.pos10.domain.mvi.ReturnDeviceIntent
import com.pos10.domain.mvi.ReturnDeviceViewModel
import com.pos10.helper.CommonUtils
import com.pos10.helper.CommonUtils.convertDateTimeFormat
import com.pos10.helper.CommonUtils.convertDateTimeToDate
import com.pos10.helper.CommonUtils.isInvalid
import com.pos10.helper.CommonUtils.showToastC
import com.pos10.helper.CustomLoader
import com.pos10.helper.NetworkUtils.isOnline
import com.pos10.helper.SharedPreference
import com.pos10.model.local.UnbindDeviceRequest
import com.pos10.model.local.UpdateRequest
import com.pos10.view.MainActivity
import com.pos10.view.SelectedRequestHolder
import com.pos10.view.SingletonObject
import com.pos10.view.navigation.Screen
import com.pos10.view.screens.qrscanner.QRScannerScreen
import kotlin.collections.forEachIndexed

@Composable
fun ReturnDeviceDetails(
    navHostController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel(),
    returnViewModel: ReturnDeviceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    (context as MainActivity).visibleStatusBar(context)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        returnViewModel.eventFlow.collect { event ->
            when (event) {
                is ReturnDeviceIntent.ShowLoader -> {
                    CustomLoader.showLoader(context)
                }

                is ReturnDeviceIntent.HideLoader -> {
                    CustomLoader.hideLoader()
                }

                is ReturnDeviceIntent.NavigateToUpload -> {
                    navHostController.navigate(Screen.UploadDocumentsScreen.route)
                }

                is ReturnDeviceIntent.NavigateToDashboard -> {
                    navHostController.navigate(Screen.DashboardScreen.route)
                }

                is ReturnDeviceIntent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is ReturnDeviceIntent.ErrorToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        observerUpdate(context, viewModel, navHostController, lifecycleOwner)
    }

    val item = SelectedRequestHolder.selectedSUbWorkItemList
    val itemMain = SelectedRequestHolder.selectedItemList
    val itemChecklist = SelectedRequestHolder.selectedItemCheckList
    val conditionMap = item
        ?.requestConditions
        ?.split(",")
        ?.mapNotNull { item ->
            val parts = item.split("-")
            val id = parts.getOrNull(0)?.toIntOrNull()
            val value = parts.getOrNull(1)?.toIntOrNull()
            if (id != null && value != null) id to value else null
        }
        ?.toMap() ?: emptyMap()

    val workOrders by viewModel.workOrders.collectAsState()
    val cancelList by viewModel.allCancelList.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.updateSuccess.collect { status ->
            SingletonObject.fromWhere = "hi"
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

    BackHandler {
        navHostController.popBackStack()
    }

    val itemList = remember {
        itemChecklist
            ?.filter { it.type.toInt() == 27711 }
            ?.map {
                val isSelectedFromApi = conditionMap[it.id] == 1
                SelectableItem(
                    name = it.value,
                    id = it.id,
                    type = it.type.toInt(),
                    isSelected = isSelectedFromApi
                )
            }
            ?.toMutableStateList() ?: mutableStateListOf()
    }

    val appointment = itemMain?.appointmentDate
    val finalDate =
        if (!appointment.isNullOrEmpty() && Regex("\\d{4}-\\d{2}-\\d{2}").matches(appointment)) {
            appointment
        } else {
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
            SingletonObject.fromWhere = "Return"
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
    }

    val downloadFiles by viewModel.allFiles.collectAsState()
    val downloadList = downloadFiles.filter { it.filePath.isNotBlank() }

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
                        .clickable { navHostController.popBackStack() }
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Merchant Id:${item?.merchantCode}",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold))
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
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
                                if (item?.installationStatus.equals("1")) {
                                    showToastC(context, "Please scan the Device Barcode")
                                    viewModel.showDialogQr = true
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Image(
                            painter = painterResource(R.drawable.circleshape_checked),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (item?.installationStatus?.contains("1") == true) "De-Activate" else "Unbind",
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
                        val simNo =
                            if (item?.simnumber.isNullOrBlank()) viewModel.simno.value else item?.simnumber
                                ?: "N/A"
                        val sno =
                            if (item?.serialNo.isNullOrBlank()) viewModel.serialnumber.value else item?.serialNo
                                ?: "N/A"

                        InfoRow(label = "Serial No:", value = sno.ifBlank { "N/A" })
                        InfoRow(label = "Device Type:", value = item?.deviceType ?: "N/A")
                        InfoRow(label = "Merchant Name:", value = item?.merchantName ?: "N/A")
                        InfoRow(label = "Device ID:", value = item?.deviceNo ?: "N/A")
                        InfoRow(label = "Sim No:", value = simNo.ifBlank { "N/A" })
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    "Return Setup",
                    modifier = Modifier.padding(start = 16.dp),
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.instrument_sans_medium)),
                    color = Color.Black
                )

                Spacer(Modifier.height(4.dp))
                Text(
                    "Return Checklist",
                    modifier = Modifier.padding(start = 16.dp),
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
                    color = Color.Black
                )
                Spacer(Modifier.height(8.dp))

                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                    itemList.forEachIndexed { index, itemname ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            // 🔹 Title
                            Text(
                                text = itemname.name,
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
                                color = Color.Black
                            )


                            // 🔹 Yes / No Options
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                // ✅ YES
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable(
                                            enabled = !item?.status.equals(CommonUtils.STATUS.COMPLETED.sttausname),
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) {
                                            itemList[index] = itemname.copy(isSelected = true)
                                        }
                                ) {
                                    RadioButton(
                                        selected = itemname.isSelected == true,
                                        onClick = {
                                            itemList[index] = itemname.copy(isSelected = true)
                                        },
                                        enabled = !item?.status.equals(CommonUtils.STATUS.COMPLETED.sttausname),
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = Color(0xFF4CAF50),   // Green
                                            unselectedColor = Color.Gray
                                        )
                                    )
                                    Text("Yes")
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // ❌ NO
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable(
                                            enabled = !item?.status.equals(CommonUtils.STATUS.COMPLETED.sttausname),
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) {
                                            itemList[index] = itemname.copy(isSelected = false)
                                        }
                                ) {
                                    itemname.isSelected?.let {
                                        RadioButton(
                                            selected = !it,
                                            onClick = {
                                                itemList[index] = itemname.copy(isSelected = false)
                                            },
                                            enabled = !item?.status.equals(CommonUtils.STATUS.COMPLETED.sttausname),
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = Color.Red,
                                                unselectedColor = Color.Gray
                                            )
                                        )
                                    }
                                    Text("No")
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                if (item?.status.equals(CommonUtils.STATUS.COMPLETED.sttausname)) {
                    if (downloadList.isNotEmpty()) {
                        Text(
                            text = "Download Files",
                            fontSize = 16.sp,
                            color = Color.Black,
                            fontFamily = FontFamily(Font(R.font.instrument_sans_medium)),
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                        )

                        val downDistinctFiles =
                            downloadList.groupBy { it.originalFileName }.mapNotNull { (_, files) ->
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
                                val fileUrl = fileItem.filePath
                                val uploadType = fileItem.uploadType ?: ""

                                Text(
                                    text = "$uploadType:",
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)),
                                    color = Color.Black
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .clickable { downloadFile(context, fileUrl, fileName) }) {
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
                                            .clickable { downloadFile(context, fileUrl, fileName) })
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
                                .clickable {
                                    if (item?.installationStatus?.contains("1") == true) {
                                        showToastC(
                                            context,
                                            "Please deactivate the return process first"
                                        )
                                        return@clickable
                                    }
                                    val notAnsweredItems = itemList.filter { it.isSelected == null }
                                    if (notAnsweredItems.isNotEmpty()) {
                                        Toast.makeText(
                                            context,
                                            "Please verify the return checklist",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        SingletonObject.fromWhere = "Return"
                                        val requestString = itemList.joinToString(",") {
                                            "${it.id}-${if (it.isSelected == true) 1 else 0}"
                                        }
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
                                            itemMain?.appointmentTime ?: "04:06 pm",
                                            RequestConditions = requestString
                                        )
                                        viewModel.hitUpdateRequest(
                                            model,
                                            CommonUtils.STATUS.INPROGRESS.sttausname
                                        )
                                    }
                                }, contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Return Complete",
                                color = Color(0xFFff6900),
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.instrument_sans_regular))
                            )
                        }
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
                                .clickable { showSheet = true }, contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Return Failed",
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.instrument_sans_regular))
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
                QRScannerScreen(onQRCodeScanned = { serialNumber ->
                    try {
                        Log.d("Serial Number", serialNumber)
                        viewModel.showDialogQr = false
                        viewModel.serialnumber.value = serialNumber
                        if (serialNumber.isInvalid()) {
                            showToastC(context, "Please scan a valid QR code")
                            return@QRScannerScreen
                        }
                        if (item?.serialNo.equals(viewModel.serialnumber.value)) {
                            if (isOnline(context)) {
                                val unbindRequest = UnbindDeviceRequest(
                                    item?.deviceNo?.toInt() ?: 0,
                                    SharedPreference.get(context).userId.toInt()
                                    )
                                returnViewModel.unbindDevice(unbindRequest, item?.requestid, serialNumber, item?.simnumber)
                            } else {
                                showToastC(context, "Internet is not available")
                            }
                        } else {
                            showToastC(context, "Please scan Active device for unbind")
                        }
                    } catch (e: Exception) {
                        Log.e("TAG", "Error parsing QR: ${e.message}")
                    }
                })
            }
        }
    }
}
