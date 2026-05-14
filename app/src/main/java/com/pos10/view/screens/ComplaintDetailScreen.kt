package com.pos10.view.screens

import android.app.DownloadManager
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
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
import com.pos10.helper.CommonUtils.returnAccessToken
import com.pos10.helper.CommonUtils.showToastC
import com.pos10.helper.CustomLoader
import com.pos10.helper.EmpResource
import com.pos10.helper.ErrorUtil
import com.pos10.helper.NetworkService
import com.pos10.helper.NetworkUtils.isOnline
import com.pos10.helper.SharedPreference
import com.pos10.model.local.UpdateRequest
import com.pos10.view.MainActivity
import com.pos10.view.SelectedRequestHolder
import com.pos10.view.SingletonObject
import com.pos10.view.navigation.Screen
import com.pos10.view.signaturecapture.SignatureDialog
import com.pos10.view.signaturecapture.saveBitmapToFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.net.URL

@Composable
fun ComplaintDetailScreen(navHostController: NavHostController,
                          viewModel: AuthViewModel = hiltViewModel()) {
    val context = LocalContext.current
    (context as MainActivity).visibleStatusBar(context)
    val item = SelectedRequestHolder.selectedSUbWorkItemList
    val itemMain = SelectedRequestHolder.selectedItemList

    var desc = ""
    if (item?.status.toString().equals(CommonUtils.STATUS.COMPLETED.sttausname)) {
        desc = item?.description.toString()
    } else {
        desc = ""
    }
    var description by remember { mutableStateOf(item?.description.toString()) }

    LaunchedEffect(Unit) {
        viewModel.setRequestId(item?.requestid.toString())
        viewModel.setRequestTypeee("R")
    }

    val cancelList by viewModel.allCancelList.collectAsState()
    val downloadFiles by viewModel.allFiles.collectAsState()
    val downloadList = downloadFiles.filter { item ->
        item.filePath.contains("", ignoreCase = true)
    }
    val networkService = NetworkService.getInstance(context)

    LaunchedEffect(Unit) {
        viewModel.updateSuccess.collect { status ->
            SingletonObject.fromWhere = "Complaint"
            Log.d("TAG", "InstallationDeviceDetails: Status ---${status} ")
            when (status) {
                CommonUtils.STATUS.INPROGRESS.sttausname -> {
                    SelectedRequestHolder.selectedSUbWorkItemList?.status= CommonUtils.STATUS.INPROGRESS.sttausname
                    navHostController.navigate(Screen.UploadDocumentsScreen.route)
                }
                CommonUtils.STATUS.FAILED.sttausname -> {
                    SelectedRequestHolder.selectedSUbWorkItemList?.status= CommonUtils.STATUS.FAILED.sttausname
                    navHostController.navigate(Screen.DashboardScreen.route)
                }
            }
        }
    }

    val savedBitmap = remember { mutableStateOf<Bitmap?>(null) }
    val imageList = remember { mutableStateListOf<Uri>() }

    LaunchedEffect(downloadList) {
        if (downloadList.isNotEmpty()) {
            val lastSignature = downloadList.lastOrNull { it.filePath.contains("signature", ignoreCase = true) }

            lastSignature?.let {
            if (lastSignature.filePath.startsWith("http") == true) {
                val cacheFile = File(context.cacheDir, "signature_cached.jpg")

                if (cacheFile.exists() && !isOnline(context)) {
                    // Offline → reuse cached signature
                    val bitmap = BitmapFactory.decodeFile(cacheFile.absolutePath)
                    withContext(Dispatchers.Main) {
                        SingletonObject.savedBitmap.value = bitmap
                        savedBitmap.value = bitmap
                    }
                } else if (isOnline(context)) {
                    // Online → download and update cache
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
                                savedBitmap.value = bitmap
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
                    savedBitmap.value = bitmap
                }else{

                }
            }
        }

            imageList.clear()
            val addedNames = mutableSetOf<String>()
            downloadList.filterNot {
                it.filePath.contains("signature", ignoreCase = true)
            }.forEach { item ->
                val file = File(item.filePath)
                val uri: Uri? = when {
                    file.exists() -> file.toUri()
                    item.filePath.startsWith("http") -> Uri.parse(item.filePath)
                    else -> null
                }

                val name = item.originalFileName ?: file.name  // Prefer originalFileName, fallback to file name

                if (uri != null && !addedNames.contains(name)) {
                    imageList.add(uri)
                    addedNames.add(name)
                }
            }
        }
    }

    val appointment = itemMain?.appointmentDate
    val finalDate = if (!appointment.isNullOrEmpty() && Regex("\\d{4}-\\d{2}-\\d{2}").matches(appointment)) {
        // Already in yyyy-MM-dd format, use as is
        appointment
    } else {
        // Not in yyyy-MM-dd format → convert
        val dattee = convertDateTimeToDate(appointment ?: "18-09-2025 00:00:00")
        convertDateTimeFormat(dattee)
    }

    var showSheet by remember { mutableStateOf(false) }
    CancelRequestBottomSheet(cancelList,showSheet = showSheet,
        onDismiss = { showSheet = false },
        onSubmit = { reasonId, remark ->
            showSheet = false
            Log.d("CancelWork", "ReasonId=$reasonId, Remark=$remark")
            SingletonObject.fromWhere = "Complaint"
            val model = UpdateRequest("",
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
                reasonId,remark)

            viewModel.hitUpdateRequest(
                model,
                CommonUtils.STATUS.FAILED.sttausname)
        })

    if (viewModel.capturesignatureDialog) {
        SignatureDialog(onDismiss = { viewModel.capturesignatureDialog = false },
            onSignatureCaptured = { bitmap ->
                savedBitmap.value = bitmap
                val imageFile = saveBitmapToFile(context, bitmap)
                imageFile.let { file ->
                    val maxFileSizeInBytes = 5 * 1024 * 1024
                    val fileSizeInBytes = imageFile.length()
                    if (fileSizeInBytes > maxFileSizeInBytes) {
                        // File size exceeds 10 MB
                        showToastC(context, "Image resolution is too low. Please upload a higher-quality image.")
                    } else {
                        val fileWithExtension = File(file.parent, "${file.name}" + ".jpg")
//                        val fileWithExtension = File(file.parent, file.name + ".jpg")
                            file.copyTo(fileWithExtension, overwrite = true)
                            val requestFile =
                                fileWithExtension.asRequestBody("image/jpeg".toMediaTypeOrNull())
                            val multipartBody = MultipartBody.Part.createFormData("File",
                                fileWithExtension.name,
                                requestFile)

                            Log.d("TAGGallery", "rememberImagePicker:$multipartBody----$requestFile")
                            //send multipart
                            viewModel.setRequestType(item?.requestid ?: "")
                            viewModel.uploadDocType.value = CommonUtils.UPLOADDOCUMENTTYPE.OUTLET.type.toString()
                            viewModel.uploadFile(returnAccessToken(context),
                                SharedPreference.get(context).userId,
                                viewModel.uploadDocType.value,
                                "R",
                                fileWithExtension)
                            //  viewModel.hitUploadFileRequest(returnAccessToken(context), item?.requestid?:"",SharedPreference.get(context).userId,multipartBody)

                    }
                }
            })
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(color = Color.White)) {
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
                    .clickable {
                        navHostController.popBackStack()
                    }, )

            Spacer(Modifier.width(5.dp))
            /*Text("Complaint",
                color = Color.Black,
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)))*/
            Text("Return",
                color = Color.Black,
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)))
        }

        // Scrollable content
        Column(modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())) {
            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Log.d("TAG", "ComplaintDetailScreen:No ${item?.deviceNo}")
                Column(modifier = Modifier.padding(6.dp)) {
                    InfoRow(label = "Serial No:", value = item?.serialNo ?: "N/A")
                    InfoRow(label = "Device Type:",
                        value = item?.deviceType?.takeIf { it.isNotBlank() } ?: "N/A")
                    InfoRow(label = "Merchant Name:", value = item?.merchantName ?: "N/A")
                    InfoRow(label = "Device ID:", value = item?.deviceNo?.takeIf { it.isNotBlank() } ?: "N/A")
                    InfoRow(label = "Sim No:", value = item?.simnumber ?: "N/A")
                }
            }

            Spacer(Modifier.height(8.dp))

            Text("Remark",
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
                color = Color(0XFF000000))

            TextField(value = description.toString(),
                onValueChange = {
                    description = it
                },
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(top = 4.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFF6F6F6),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .background(color = Color.White, shape = RoundedCornerShape(8.dp)),
                placeholder = {
                    Text("Describe the issue you resolved....", color = Color.Gray)
                },
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, // Light gray background
                    unfocusedContainerColor = Color.White,
                    cursorColor = Color.Black,
                    focusedTextColor = Color.Black,
                    unfocusedPlaceholderColor = Color.Gray,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent))

            Spacer(Modifier.height(12.dp))

            //add download list here

            Log.d("TAG", "ComplaintDetailScreen: Download ${downloadList}")
            if (item?.status.toString().equals(CommonUtils.STATUS.COMPLETED.sttausname)) {
                if (!downloadList.isNullOrEmpty()) {
                    Text(text = "Download Files",
                        fontSize = 16.sp,
                        color = Color.Black,
                        fontFamily = FontFamily(Font(R.font.instrument_sans_medium)),
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp))

                    val downDistinctFiles = downloadList
                        .groupBy { it.originalFileName }
                        .mapNotNull { (_, files) ->
                            // Pick the one that contains the full URL (http/https)
                            files.firstOrNull { it.filePath.startsWith("http") }
                        }

                    LazyColumn(modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .padding(horizontal = 16.dp)) {
                        items(downDistinctFiles) { fileItem ->
                            Log.d("TAG", "ComplaintDetailScreen: downl----${fileItem.filePath} ")
                            val fileName = fileItem.originalFileName ?: "File"
                            val fileUrl = fileItem.filePath ?: ""
                            val uploadType = fileItem.uploadType?:""

                            Text(text = "$uploadType:",
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)),
                                color = Color.Black,
                                modifier = Modifier.weight(1f))
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable {
                                        downloadFile(context, fileUrl, fileName)
                                    }) {
                                // Thumbnail
                                if (fileName.endsWith(".pdf", true)) {
                                    Image(painter = painterResource(id = R.drawable.pdf),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(6.dp)))
                                } else {
                                    AsyncImage(model = fileUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(
                                                1.dp,
                                                Color(0xFFF6F6F6),
                                                RoundedCornerShape(8.dp)
                                            ))

                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // File name
                                Text(text = fileName,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
                                    color = Color.Black,
                                    modifier = Modifier.weight(1f))

                                Image(painter = painterResource(R.drawable.ic_download),
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
                }
                Spacer(Modifier.height(12.dp))
            }

            if (item?.status.toString().equals(CommonUtils.STATUS.COMPLETED.sttausname)) {
            } else {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White))
                {
                    Spacer(Modifier.height(50.dp))

                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp) // space between buttons
                    ) {
                        // Return Resolved
                        /*Text(text = "Complaint Resolved",
                            fontSize = 16.sp,
                            color = Color(0xFFff6900),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFff6900),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .background(color = Color.White, shape = RoundedCornerShape(12.dp))
                                .padding(horizontal = 25.dp, vertical = 8.dp)
                                .clickable {
                                    val model = UpdateRequest(
                                        description,
                                        "1900-01-01",
                                        false,
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
                                    //viewModel.hitUpdate(returnAccessToken(context), model)
                                    viewModel.hitUpdateRequest(
                                        returnAccessToken(context),
                                        model,
                                        CommonUtils.STATUS.INPROGRESS.sttausname
                                    )
                                },
                            fontFamily = FontFamily(Font(R.font.instrument_sans_medium)))*/

                        Text(text = "Return Resolved",
                            fontSize = 16.sp,
                            color = Color(0xFFff6900),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFff6900),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .background(color = Color.White, shape = RoundedCornerShape(12.dp))
                                .padding(horizontal = 25.dp, vertical = 8.dp)
                                .clickable {
                                    val model = UpdateRequest(
                                        description,
                                        "1900-01-01",
                                        false,
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
                                    //viewModel.hitUpdate(returnAccessToken(context), model)
                                    viewModel.hitUpdateRequest(
                                        model,
                                        CommonUtils.STATUS.INPROGRESS.sttausname
                                    )
                                },
                            fontFamily = FontFamily(Font(R.font.instrument_sans_medium)))

                        // Mark as Unresolved
                        Text(text = "Mark as Unresolved",
                            fontSize = 16.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = 1.dp,
                                    color = Color.Gray,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .background(color = Color.White, shape = RoundedCornerShape(12.dp))
                                .padding(horizontal = 25.dp, vertical = 8.dp)
                                .clickable {
                                    // Handle unresolved click
                                    showSheet = true
                                },
                            fontFamily = FontFamily(Font(R.font.instrument_sans_medium)))
                    }

                }
            }
        }
    }
}

fun downloadFile(context: Context, fileUrl: String, fileName: String) {
    Log.d("TAG", "downloadFile: DownloadUrl-----${fileUrl}")
    val request = DownloadManager.Request(Uri.parse(fileUrl)).setTitle(fileName)
        .setDescription("Downloading $fileName")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        .setAllowedOverMetered(true).setAllowedOverRoaming(true)

    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val downloadId = downloadManager.enqueue(request)

    // Launch coroutine to track progress
    CoroutineScope(Dispatchers.IO).launch {
        var isDownloading = true
        val query = DownloadManager.Query().setFilterById(downloadId)

        while (isDownloading) {
            val cursor = downloadManager.query(query)
            if (cursor != null && cursor.moveToFirst()) {
                val bytesDownloaded =
                    cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val bytesTotal =
                    cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                if (bytesTotal > 0) {
                    val progress = (bytesDownloaded * 100L) / bytesTotal
                    Log.d("Download", "Progress: $progress%")
                }

                val status =
                    cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    isDownloading = false
                    withContext(Dispatchers.Main) {
                        //$file downloaded
                        showToastC(context, "File downloaded")
                    }
                } else if (status == DownloadManager.STATUS_FAILED) {
                    isDownloading = false
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            cursor?.close()
            delay(500)
        }
    }
    showToastC(context, "Downloading $fileName")
}
