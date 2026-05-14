package com.pos10.view

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import com.pos10.R
import com.pos10.domain.AuthViewModel
import com.pos10.helper.CommonUtils
import com.pos10.helper.CommonUtils.convertDateTimeFormat
import com.pos10.helper.CommonUtils.convertDateTimeToDate
import com.pos10.helper.CommonUtils.returnAccessToken
import com.pos10.helper.CommonUtils.showToastC
import com.pos10.helper.CustomLoader
import com.pos10.helper.EmpResource
import com.pos10.helper.ErrorUtil
import com.pos10.helper.NetworkService
import com.pos10.helper.NetworkUtils.isOnline
import com.pos10.helper.SharedPreference
import com.pos10.model.local.UpdateRequest
import com.pos10.model.remote.DownloadFileResponse
import com.pos10.view.navigation.Screen
import com.pos10.view.screens.InfoRow
import com.pos10.view.screens.UploadBox
import com.pos10.view.screens.rememberImagePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

private const val TAG = "UploadDocumentsScreen"

@Composable
fun UploadDocumentsScreen(navHostController: NavHostController,
                          viewModel: AuthViewModel = hiltViewModel()){

    val context = LocalContext.current
    (context as MainActivity).visibleStatusBar(context)
    val lifecycleOwner = LocalLifecycleOwner.current
    val networkService = NetworkService.getInstance(context)

    val item  = SelectedRequestHolder.selectedSUbWorkItemList
    val itemMain  = SelectedRequestHolder.selectedItemList

    LaunchedEffect(Unit) {
        viewModel.setRequestId(item?.requestid.toString())
        viewModel.setRequestTypeee("R")
    }
    val downloadFiles by viewModel.allFiles.collectAsState()
    val downloadList = downloadFiles.filter { item ->
        item.filePath.isNotBlank()
    }

    LaunchedEffect(Unit) {
        observerCompleteProcess(context, lifecycleOwner, navHostController, viewModel)
    }

    Log.d(TAG, "UploadDocumentsScreen:Item ---$downloadList ")

    val imageList = remember { mutableStateListOf<Uri>() }
    LaunchedEffect(downloadList) {
        if (downloadList.isNotEmpty()) {
            val lastSignature = downloadList.lastOrNull { it.filePath.contains("signature", ignoreCase = true) }

            lastSignature?.let {
                if (lastSignature.filePath.startsWith("http") == true) {
                    val cacheFile = File(context.cacheDir, "signature_cached.jpg")

                    if (cacheFile.exists() && !isOnline(context)) {
                        //Offline → reuse cached signature
                        val bitmap = BitmapFactory.decodeFile(cacheFile.absolutePath)
                        withContext(Dispatchers.Main) {
                            SingletonObject.savedBitmap.value = bitmap
                        }
                    } else if (isOnline(context)) {
                        //Online → download and update cache
                        withContext(Dispatchers.IO) {
                            try {
                                val url = URL(lastSignature.filePath)
                                url.openStream().use { input ->
                                    cacheFile.outputStream().use { output ->
                                        input.copyTo(output)
                                    }
                                }

                                val bitmap = BitmapFactory.decodeFile(cacheFile.absolutePath)

                                withContext(Dispatchers.Main) {
                                    SingletonObject.savedBitmap.value = bitmap
                                }
                            } catch (e: Exception) {
                                Log.e("SIGNATURE", "Download failed: ${e.message}", e)
                            }
                        }
                    } else {
                        Log.e("SIGNATURE", "No internet and no cached file")
                    }
                } else {
                    // Local file path
                    val file = File(lastSignature?.filePath)
                    if (file.exists()) {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        SingletonObject.savedBitmap.value = bitmap

                    }else{

                    }
                }
            }

            fun toUri(item: DownloadFileResponse.Data): Uri? {
                val file = File(item.filePath)
                return when {
                    file.exists() -> file.toUri()
                    item.filePath.startsWith("http") -> Uri.parse(item.filePath)
                    else -> null
                }
            }

            //Assign per-type single Uri

            SingletonObject.uploadSerialId.value =
                downloadList.firstOrNull() { matchesUploadType(it, CommonUtils.UPLOADDOCUMENTTYPE.SERIALID) }
                    ?.let { toUri(it) }

            SingletonObject.uploadIdleId.value =
                downloadList.firstOrNull() { matchesUploadType(it, CommonUtils.UPLOADDOCUMENTTYPE.IDLE) }
                    ?.let { toUri(it) }

            SingletonObject.uploadCashierId.value =
                downloadList.firstOrNull() { matchesUploadType(it, CommonUtils.UPLOADDOCUMENTTYPE.CASHIER) }
                    ?.let { toUri(it) }

        }else{
            SingletonObject.uploadCashierId.value=null
            SingletonObject.uploadIdleId.value =null
            SingletonObject.uploadSerialId.value =null
        }
    }

    if (viewModel.showDialogActive) {
        CustomDialogBoxActive(viewModel){
            viewModel.showDialogActive = false
                if (viewModel.terminalStatus.contains("Pending")) {
                    viewModel.showDialogActive = false
                    navHostController.navigate(Screen.DashboardScreen.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                } else {
                    val itemMain = SelectedRequestHolder.selectedItemList
                    val appointment = itemMain?.appointmentDate
                    val finalDate =
                        if (!appointment.isNullOrEmpty() && Regex("\\d{4}-\\d{2}-\\d{2}").matches(
                                appointment)) {
                            // Already in yyyy-MM-dd format, use as is
                            appointment
                        } else {
                            // Not in yyyy-MM-dd format → convert
                            val dattee = convertDateTimeToDate(appointment ?: "18-09-2025 00:00:00")
                            convertDateTimeFormat(dattee)
                        }
                    Log.d(TAG, "UploadDocumentsScreen: Check $finalDate")
                    val model = UpdateRequest(item?.description ?: "",
                        "1900-01-01",
                        true,
                        "",
                        SharedPreference.get(context).userId.toInt(),
                        0,
                        0,
                        item?.requestid?.toInt() ?: 0,
                        CommonUtils.STATUS.COMPLETED.type,
                        0,
                        finalDate,
                        itemMain?.appointmentTime ?: "04:06 pm")

                    Log.d(TAG, "UploadDocumentsScreen: CheckModel $model")

                    viewModel.hitUpdateRequest(
                        model,
                        CommonUtils.STATUS.COMPLETED.sttausname)
                }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.updateSuccess.collect { status ->
            Log.d("TAG", "InstallationDeviceDetails: Status ---${status} ")
            when (status) {
                CommonUtils.STATUS.INPROGRESS.sttausname -> {

                }

                CommonUtils.STATUS.FAILED.sttausname -> {

                }

                CommonUtils.STATUS.COMPLETED.sttausname -> {
                    if (item?.requesttype?.contains("Replacement") == true) {
                        viewModel.updateSubRequestTypeToInstallation(itemMain?.workid ?: 0, item.requestid)
                    } else {
                        navHostController.navigate(Screen.DashboardScreen.route) {
                            popUpTo(0) {
                                inclusive = true
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.localDbUpdateSuccess.collect { isSuccess ->
            if (isSuccess) {
                SelectedRequestHolder.selectedSUbWorkItemList = SelectedRequestHolder.selectedSUbWorkItemList?.copy(requesttype = "Installation")
                navHostController.navigate(Screen.JobInstallationDetailScreen.route) {
                    popUpTo(0) {
                        inclusive = true
                    }
                }
            } else {
                navHostController.navigate(Screen.DashboardScreen.route) {
                    popUpTo(0) {
                        inclusive = true
                    }
                }
            }
        }
    }

    val imagePicker = rememberImagePicker(
        context = context,
        viewModel = viewModel,
        navHostController = navHostController,
        lifecycleOwner = lifecycleOwner,
        item = item,
        itemMain=itemMain,
        onClick = { isPdf ->
            Log.d("Upload", "Is PDF: $isPdf")
        }, )

    BackHandler {
        val currentRoute = navHostController.currentBackStackEntry?.destination?.route
        Log.d("BackHandler", "Current route: $currentRoute")

        if (currentRoute == Screen.JobUploadScreen.route) {
            Log.d("BackHandler", "Current route:true")
            if(item?.requesttype?.contains("Installation") == true) {
                navHostController.navigate(Screen.JobInstallationDetailScreen.route) {
                    popUpTo(Screen.JobUploadScreen.route) { inclusive = false }
                    launchSingleTop = true
                }
            }else{
                navHostController.navigate(Screen.PaperRollScreen.route) {
                    popUpTo(Screen.JobUploadScreen.route) { inclusive = false }
                    launchSingleTop = true
                }
            }
        } else {
            Log.d("BackHandler", "Current route:False")
            navHostController.popBackStack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.White)
                .padding(top = 40.dp, bottom = 10.dp)
                .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.ic_back_black),
                    contentDescription = "",
                    modifier = Modifier
                        .width(16.dp)
                        .clickable(indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {

                            val currentRoute =
                                navHostController.currentBackStackEntry?.destination?.route

                            if (currentRoute == Screen.JobUploadScreen.route) {
                                if (item?.requesttype?.contains("Installation") == true) {
                                    navHostController.navigate(Screen.JobInstallationDetailScreen.route) {
                                        popUpTo(Screen.JobUploadScreen.route) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                } else {
                                    navHostController.navigate(Screen.PaperRollScreen.route) {
                                        popUpTo(Screen.JobUploadScreen.route) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                }
                            } else {
                                Log.d("BackHandler", "Current route:False")
                                navHostController.popBackStack()
                            }
                        },
                )
                Spacer(Modifier.width(8.dp))
                Text("Upload Document",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)))
            }

            // Scrollable content
            Column(modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .background(color = Color.White)) {
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        InfoRow(label = "Device Type:", value = item?.deviceType.toString().ifBlank { "N/A" })
                        InfoRow(label = "Merchant Name:", value = item?.merchantName ?: "N/A")
                        InfoRow(label = "Device ID:", value = item?.deviceNo.toString().ifBlank { "N/A" })
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically)
                {
                    Text("Serial Number",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 16.dp),
                        fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
                        color = Color.Black)

                    Spacer(Modifier.weight(1f))

                    UploadBox(
                        viewModel = viewModel,
                        modifier = Modifier,
                        selectedImageUri = SingletonObject.uploadSerialId.value,
                        imageResId = R.drawable.upload_image,
                        title = "Upload",
                        subtitle = CommonUtils.UPLOADDOCUMENTTYPE.SERIALID.type.toString(),
                        onClick = {
                            imagePicker { uri ->
                                viewModel.uploadType = 3
                                SingletonObject.uploadSerialId.value = null
                                SingletonObject.uploadSerialId.value=uri
                            }
                        },
                        onRemoveClick = {
                            val serialUris = downloadList
                                .filter { matchesUploadType(it, CommonUtils.UPLOADDOCUMENTTYPE.SERIALID) }
                                .mapNotNull { toUri(it) }

                            if (serialUris.isNotEmpty()) {
                                serialUris.forEach { uri ->
                                    Log.d("TAG", "JobUploadDocScren:WorkScreen---${viewModel.uploadRequestType.value} --${viewModel.uploadDocType.value} ")
                                    viewModel.removeImage(uri = uri,
                                        requestId = item?.requestid?.toInt() ?: 0,
                                        uploadType = CommonUtils.UPLOADDOCUMENTTYPE.SERIALID.type.toString(),
                                        token = returnAccessToken(context),
                                        requestTye = "R")
                                }
                                SingletonObject.uploadSerialId.value = null
                            }
                        }
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Idle Screen",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 16.dp),
                        fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
                        color = Color.Black)

                    Spacer(Modifier.weight(1f))
                    UploadBox(
                        viewModel = viewModel,
                        modifier = Modifier,
                        selectedImageUri = SingletonObject.uploadIdleId.value,
                        imageResId = R.drawable.upload_image,
                        title = "Upload",
                        subtitle = CommonUtils.UPLOADDOCUMENTTYPE.IDLE.type.toString(),
                        onClick = {
                            imagePicker { uri ->
                                viewModel.uploadType = 4
                                SingletonObject.uploadIdleId.value = null
                                SingletonObject.uploadIdleId.value=uri
                            }
                        },
                        onRemoveClick = {
                            val serialUris = downloadList
                                .filter { matchesUploadType(it, CommonUtils.UPLOADDOCUMENTTYPE.IDLE) }
                                .mapNotNull { toUri(it) }

                            if (serialUris.isNotEmpty()) {
                                serialUris.forEach { uri ->
                                    Log.d("TAG", "JobUploadDocScren:WorkScreen---${viewModel.uploadRequestType.value} --${viewModel.uploadDocType.value} ")
                                    viewModel.removeImage(uri = uri,
                                        requestId = item?.requestid?.toInt() ?: 0,
                                        uploadType = CommonUtils.UPLOADDOCUMENTTYPE.IDLE.type.toString(),
                                        token = returnAccessToken(context),
                                        requestTye = "R")
                                }
                                SingletonObject.uploadIdleId.value = null
                            }
                        }
                    )
                }
                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Cashier Screen",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 16.dp),
                        fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
                        color = Color.Black)

                    Spacer(Modifier.weight(1f))
                    UploadBox(
                        viewModel = viewModel,
                        modifier = Modifier,
                        selectedImageUri = SingletonObject.uploadCashierId.value,
                        imageResId = R.drawable.upload_image,
                        title = "Upload",
                        subtitle = CommonUtils.UPLOADDOCUMENTTYPE.CASHIER.type.toString(),
                        onClick = {
                            imagePicker { uri ->
                                viewModel.uploadType = 5
                                SingletonObject.uploadCashierId.value = null
                                SingletonObject.uploadCashierId.value=uri
                            }
                        },
                        onRemoveClick = {
                            val serialUris = downloadList
                                .filter { matchesUploadType(it, CommonUtils.UPLOADDOCUMENTTYPE.CASHIER) }
                                .mapNotNull { toUri(it) }

                            if (serialUris.isNotEmpty()) {
                                serialUris.forEach { uri ->
                                    Log.d("TAG", "JobUploadDocScren:WorkScreen---${viewModel.uploadRequestType.value} --${viewModel.uploadDocType.value} ")
                                    viewModel.removeImage(uri = uri,
                                        requestId = item?.requestid?.toInt() ?: 0,
                                        uploadType = CommonUtils.UPLOADDOCUMENTTYPE.CASHIER.type.toString(),
                                        token = returnAccessToken(context),
                                        requestTye = "R")
                                }
                                SingletonObject.uploadCashierId.value = null
                            }
                        }
                    )
                }

                Spacer(Modifier.height(24.dp))
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp)
                    .height(45.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF4CAF50),
                        Color(0xFF81C784))))
                    .clickable {
                        Log.d(TAG,
                            "UploadDocumentsScreen: Upload ${SingletonObject.uploadSerialId.value}," + "${SingletonObject.uploadIdleId.value},${SingletonObject.uploadSerialId.value}")

                        if (item?.requesttype.toString()
                                .contains(CommonUtils.REQUESTTYPE.INSTALLATION.title)) {
                            if (SingletonObject.uploadSerialId.value == null) {
                                showToastC(context, "Please upload Serial number")
                                return@clickable
                            }
                            if (SingletonObject.uploadIdleId.value == null) {
                                showToastC(context, "Please upload Idle Screen")
                                return@clickable
                            }
                            if (SingletonObject.uploadCashierId.value == null) {
                                showToastC(context, "Please upload Cashier Screen")
                                return@clickable
                            }
                        }/* else if (item?.requesttype.toString()
                                .contains(CommonUtils.REQUESTTYPE.COMPLAINT.title)) {
                            if (SingletonObject.uploadSerialId.value == null) {
                                showToastC(context, "Please upload Serial number")
                                return@clickable
                            }
                        }*/
                        viewModel.hitTerminalStatus(returnAccessToken(context),
                            item?.deviceNo ?: "")
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Complete Process",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

        }
    }
}

fun matchesUploadType(item: DownloadFileResponse.Data,
    targetType: CommonUtils.UPLOADDOCUMENTTYPE): Boolean {
    Log.d(TAG, "matchesUploadType: Item=${item.uploadType} TargetType=${targetType.type} TargetName=${targetType.uploadType}")
    val itemTypeStr = item.uploadType?.toString()?.trim() ?: ""
    val targetTypeStr = targetType.type.toString().trim()
    val targetTypeNameStr = targetType.uploadType?.trim() ?: ""

    return itemTypeStr.equals(targetTypeStr, ignoreCase = true) ||
            itemTypeStr.equals(targetTypeNameStr, ignoreCase = true)
}


fun toUri(item: DownloadFileResponse.Data): Uri? {
    val file = File(item.filePath)

    return when {
        //Local file exists
        file.exists() -> file.toUri()

        //Remote URL
        item.filePath.startsWith("http") -> Uri.parse(item.filePath)
        else -> null
    }
}

fun observerCompleteProcess(context: MainActivity,
                     lifecycleOwner: LifecycleOwner,
                     navHostController: NavHostController,
                     viewModel: AuthViewModel) {

    viewModel.terminalstatusLiveData.observe(lifecycleOwner) {
        when (it) {
            is EmpResource.Failure -> {
                it.throwable?.let { it1 -> ErrorUtil.handlerGeneralError(context, it1) }
                CustomLoader.hideLoader()
            }

            EmpResource.Loading -> {
                CustomLoader.showLoader(context)
            }
            is EmpResource.Success-> {
                CustomLoader.hideLoader()
                viewModel.terminalStatus = it.value.data.terminalStatus
                viewModel.showDialogActive =true
            }
        }
    }
}

@Composable
fun CustomDialogBoxActive(
    viewModel: AuthViewModel,
    onClick: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = { }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.White, shape = RoundedCornerShape(12.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = if (viewModel.terminalStatus.contains("Pending")) {
                    painterResource(id = R.drawable.close_ic) // inactive
                } else {
                    painterResource(id = R.drawable.success)   // active
                },
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (viewModel.terminalStatus.contains("Pending"))
                    "Setup is Inactive"
                else
                    "Setup is Active",
                fontFamily = FontFamily(Font(R.font.instrument_sans_bold)),
                fontSize = 16.sp,
                color = Color(0xff0E1C21)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 0.5.dp,
                        color = Color(0xFFd5d5d5),
                        shape = RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onClick() }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "OK",
                    fontFamily = FontFamily(Font(R.font.instrument_sans_bold)),
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }
    }
}
