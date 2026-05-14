package com.pos10.view.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.pos10.R
import com.pos10.domain.AuthViewModel
import com.pos10.helper.CommonUtils
import com.pos10.helper.CommonUtils.bitmapToUri
import com.pos10.helper.CommonUtils.compressImageFile
import com.pos10.helper.CommonUtils.returnAccessToken
import com.pos10.helper.CommonUtils.showToastC
import com.pos10.helper.CustomHttpClient
import com.pos10.helper.NetworkService
import com.pos10.helper.NetworkUtils.isOnline
import com.pos10.helper.SharedPreference
import com.pos10.model.remote.GetWorkListResponse
import com.pos10.util.uriToFile
import com.pos10.view.MainActivity
import com.pos10.view.SelectedRequestHolder
import com.pos10.view.SingletonObject
import com.pos10.view.navigation.Screen
import com.pos10.view.signaturecapture.SignatureDialog
import com.pos10.view.signaturecapture.saveBitmapToFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

data class ImageItem(
    val id: String,
    val uri: Uri
)

@Composable
fun JobUploadDocScren(
    navHostController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {

    val context = LocalContext.current
    (context as MainActivity).visibleStatusBar(context)
    val lifecycleOwner = LocalLifecycleOwner.current
    val networkService = NetworkService.getInstance(context)
    val item = SelectedRequestHolder.selectedSUbWorkItemList
    val itemMain = SelectedRequestHolder.selectedItemList

    LaunchedEffect(Unit) {
        viewModel.setRequestId(itemMain?.workid.toString())
        viewModel.setRequestTypeee("W")
        viewModel.refreshFiles(itemMain?.workid.toString(), "W")
    }

    val downloadFiles by viewModel.allFiles.collectAsState()
    val downloadList = downloadFiles.filter { item ->
        item.filePath.isNotBlank()
    }

    Log.d("TAG", "JobUploadDocScren: Download -----$downloadList")
    val savedBitmap = remember { mutableStateOf<Bitmap?>(null) }
    val signatureId = remember { mutableStateOf("") }
    // val imageList = remember { mutableStateListOf<Uri>() }
    val imageList = remember { mutableStateListOf<ImageItem>() }
    LaunchedEffect(downloadList) {
        if (downloadList.isNotEmpty()) {
            val lastSignature =
                downloadList.lastOrNull { it.filePath.contains("signature", ignoreCase = true) }

            lastSignature?.let {
                if (lastSignature.filePath.startsWith("http") == true) {
                    val cacheFile = File(context.cacheDir, "signature_cached.jpg")

                    if (cacheFile.exists() && !isOnline(context)) {
                        // ✅ Offline → reuse cached signature
                        val bitmap = BitmapFactory.decodeFile(cacheFile.absolutePath)
                        withContext(Dispatchers.Main) {
                            SingletonObject.savedBitmap.value = bitmap
                            savedBitmap.value = bitmap
                            signatureId.value = lastSignature.id
                        }
                    } else if (isOnline(context)) {
                        //  Online → download and update cache
                        withContext(Dispatchers.IO) {
                            try {

                                val client =
                                    CustomHttpClient.ImageOkHttpProvider.getUnsafeOkHttpClient()

                                val request = Request.Builder()
                                    .url(lastSignature.filePath)
                                    .build()

                                val response = client.newCall(request).execute()

                                if (!response.isSuccessful) {
                                    Log.e("SIGNATURE", "Download failed: ${response.code}")
                                    return@withContext
                                }

                                val inputStream = response.body?.byteStream()

                                inputStream?.use { input ->
                                    cacheFile.outputStream().use { output ->
                                        input.copyTo(output)
                                    }
                                }

                                val bitmap = BitmapFactory.decodeFile(cacheFile.absolutePath)

                                withContext(Dispatchers.Main) {
                                    SingletonObject.savedBitmap.value = bitmap
                                    savedBitmap.value = bitmap
                                    signatureId.value = lastSignature.id
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
                        signatureId.value = lastSignature.id
                    } else {
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

                val name = item.originalFileName
                    ?: file.name  // ✅ Prefer originalFileName, fallback to file name

                if (uri != null && !addedNames.contains(name)) {
                    imageList.add(ImageItem(id = item.id, uri = uri))
                    addedNames.add(name)
                }
            }
        }
    }


    val uploadImageUriBusiness1 by SingletonObject.uploadImageUriBusiness1

// var uploadImageUriBusiness1 by remember { mutableStateOf<Uri?>(null) }
    Log.d("BackHandler", "UploadImagge: Imagelist $imageList")

    BackHandler {
        navHostController.popBackStack()
    }

    /* LaunchedEffect(Unit) {
         observerCompletedJOb(context,viewModel,navHostController, lifecycleOwner)
     }*/

    if (viewModel.capturesignatureDialog) {
        SignatureDialog(
            onDismiss = { viewModel.capturesignatureDialog = false },
            onSignatureCaptured = { bitmap ->
                savedBitmap.value = bitmap
                SingletonObject.savedBitmap.value = bitmap
                uploadDocx(context, viewModel, item, bitmap, itemMain)
            })
    }

    Log.d("TAG", "JobUploadDocScren:Sign  ${SingletonObject.savedBitmap.value} ")

    val imagePicker = rememberImagePicker(
        context = context,
        viewModel = viewModel,
        navHostController = navHostController,
        lifecycleOwner = lifecycleOwner,
        item = item,
        itemMain = itemMain,
        onClick = { isPdf ->
            Log.d("Upload", "Is PDF: $isPdf")
        },
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            navHostController.popBackStack()
                        },
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Upload Document",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold))
                )
            }

            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .background(color = Color.White)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        InfoRow(label = "Merchant Name:", value = itemMain?.merchantName ?: "N/A")
                        InfoRow(label = "MerchantCode:", value = itemMain?.merchantCode ?: "N/A")
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    "Upload Device Photo",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 16.dp),
                    fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
                    color = Color.Black
                )

                Spacer(Modifier.height(16.dp))

                /* UploadBox(
                     viewModel = viewModel,
                     modifier = Modifier.fillMaxWidth(),
                     uploadImageUriBusiness1,
                     imageResId = R.drawable.upload_image,
                     title = "Supported file: JPEG, PNG",
                     subtitle = "",
                     onClick = {
                         imagePicker { uri ->
                             viewModel.uploadType = 1
                           //  uploadImageUriBusiness1 = uri
                             SingletonObject.uploadImageUriBusiness1.value=uri
                             Log.d("abhi", "Document 1 URI: $uri")
                         }
                     }
                 )

                 Spacer(Modifier.height(16.dp))*/
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            color = Color(0xffF3F6F7),
                            shape = RoundedCornerShape(12.dp),
                        )
                        .clickable {
                            if (imageList.size < 5) {
                                viewModel.uploadDocType.value =
                                    CommonUtils.UPLOADDOCUMENTTYPE.OUTLET.type.toString()
                                viewModel.uploadRequestType.value = "W"
                                imagePicker { uri ->

                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "You can upload max 5 pictures",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 1.dp, color = Color(0xFFF6F6F6))
                            .background(color = Color.White)
                    ) {
                        CustomImage(imageRes = R.drawable.upload_image)

                        CustomText(
                            text = "Supported file: JPEG, PNG",
                            style = TextStyle(
                                color = Color(0xff656771),
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.instrument_sans_regular))
                            )
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                LazyRow {
//                    items(imageList) { uri ->
                    items(imageList) { imageItem ->
                        val uri = imageItem.uri
                        val fileId = imageItem.id

                        val imageSize = 100.dp

                        Box(
                            modifier = Modifier
                                .size(imageSize)
                                .padding(start = 8.dp, end = 8.dp)
                        ) {

                            val fileName = getFileNameFromUri(context, uri ?: Uri.EMPTY)

                            if (fileName.toString().contains(".pdf")) {
                                AsyncImage(
                                    model = R.drawable.pdf,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .width(65.dp)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            } else {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }

                            CustomImage(
                                imageRes = R.drawable.close_ic,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .clickable {
                                        imageList.remove(imageItem)
                                        viewModel.removeImage(
                                            uri = uri,
                                            requestId = fileId.toInt(),
                                            uploadType = "3906",
                                            token = returnAccessToken(context),
                                            requestTye = "W"
                                        )
                                    })
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                if (savedBitmap.value == null) {
                    CustomText(
                        text = "Capture Signature",
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                            .clickable {
                                viewModel.capturesignatureDialog = true
                            },
                        style = TextStyle(
                            color = Color(0xff7F9CA6),
                            fontSize = 16.sp,
                            textDecoration = TextDecoration.Underline,
                            fontFamily = FontFamily(Font(R.font.instrument_sans_medium))
                        )
                    )
                }

                Spacer(Modifier.height(8.dp))

                savedBitmap.value?.let { bmp ->
                    Box(
                        modifier = Modifier
                            .height(140.dp)
                            .padding(top = 5.dp, start = 16.dp, end = 16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                1.dp,
                                color = Color(0xffEDF1F3),
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Image(
                            painter = BitmapPainter(bmp.asImageBitmap()),
                            contentDescription = "Captured Signature",
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                        )

                        Image(
                            painter = painterResource(R.drawable.close_ic),
                            contentDescription = "Remove",
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-6).dp, y = 6.dp)
                                .background(Color.White, CircleShape)
                                .border(1.dp, Color.LightGray, CircleShape)
                                .size(20.dp)
                                .clickable {
                                    val uri = bitmapToUri(context, bmp)
                                    savedBitmap.value = null
                                    SingletonObject.savedBitmap.value = null
                                    viewModel.removeImage(
                                        uri = uri,
                                        requestId = signatureId.value.toInt(),
                                        uploadType = "3906",
                                        token = returnAccessToken(context),
                                        requestTye = "W"
                                    )
                                    signatureId.value = ""
                                }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp)
                        .height(45.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF4CAF50),
                                    Color(0xFF81C784)
                                )
                            )
                        )
                        .clickable {
                            Log.d(
                                "TAG",
                                "JobUploadDocScren:Sscc ${SingletonObject.uploadImageUriBusiness1.value}----${SingletonObject.savedBitmap.value}"
                            )
                            if (imageList.size == 0) {
                                showToastC(context, "Please upload document")
                                return@clickable
                            }
                            if (savedBitmap.value == null) {
                                showToastC(context, "Please capture signature")
                                return@clickable
                            }

                            val online = networkService.isOnline.value ?: false
                            if (online) {
                                navHostController.navigate(Screen.PaperRollConfirmationCodeScreen.route)
                            } else {
                                showToastC(context, "Network required to confirm the job")
                            }
//                            navHostController.navigate(Screen.PaperRollConfirmationCodeScreen.route)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Confirm",
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


@Composable
fun UploadBox(
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier,
    selectedImageUri: Uri?,
    imageResId: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {},
    onRemoveClick: () -> Unit = {}
) {

    Column(
        modifier = modifier
            .padding(top = 5.dp, start = 16.dp, end = 24.dp)
            .size(width = 100.dp, height = 80.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, color = Color(0xfff6f6f6), shape = RoundedCornerShape(8.dp))
            .clickable {
                if (selectedImageUri == null) {
                    viewModel.uploadDocType.value =
                        subtitle //it is documenttype (serial,idle, cashier)
                    viewModel.uploadRequestType.value = "R"
                    onClick()
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Log.d("abhi", "UploadBox: image -selectedImageUri -${selectedImageUri.toString()} ")

        val context = LocalContext.current

        if (selectedImageUri != null) {
            Log.d("TAG", "UploadBox:Inside----$selectedImageUri ")
            val fileName = getFileNameFromUri(context, selectedImageUri)
            val fileExtension = fileName?.substringAfterLast('.', "")
            Log.d("TAG", "PDF File Name: $fileName")
            Log.d("TAG", "PDF File Extension: $fileExtension")

            if (fileName != null && !fileName.toString()
                    .isNullOrEmpty() || selectedImageUri != null
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp) // image height
                        .clip(RoundedCornerShape(8.dp))
                ) {

                    if (fileName?.contains("/document%") == true || fileName?.contains(".pdf") == true || fileName?.contains(
                            ".xls"
                        ) == true || fileName?.contains(".xlsx") == true
                    ) {
                        AsyncImage(
                            model = R.drawable.pdf,
                            contentDescription = null,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Close icon overlay
                    Image(
                        painter = painterResource(R.drawable.close_ic),
                        contentDescription = "Remove",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 0.dp, y = (-0).dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, Color.LightGray, CircleShape)
                            .size(20.dp)
                            .clickable {
                                onRemoveClick()
                            }
                    )
                }

            }
        } else {
            Log.d("TAGKa", "UploadBox: 3")
            Spacer(modifier = Modifier.height(30.dp))
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = null
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun rememberImagePicker(
    context: Context,
    viewModel: AuthViewModel,
    navHostController: NavHostController,
    lifecycleOwner: LifecycleOwner,
    item: GetWorkListResponse.Data.Wo.WoRequest? = null,
    itemMain: GetWorkListResponse.Data.Wo? = null,
    onClick: (Boolean) -> Unit
): (onImageSelected: (Uri) -> Unit) -> Unit {

    var onImageSelected by remember { mutableStateOf<(Uri) -> Unit>({}) }
    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }

    // Camera launcher
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                cameraImageUri.value?.let { uri ->
                    onImageSelected(uri)
                    val file = uriToFile(context, uri)

                    file?.let {
                        val maxFileSize = 10 * 1024 * 1024 // 10 MB limit for safety
                        if (it.length() > maxFileSize) {
                            showToastC(context, "Image is too large.")
                        } else {
                            val fileWithExt = File(it.parent, "${it.name}.jpg")
                            it.copyTo(fileWithExt, overwrite = true)

                            // ✅ Compress before upload
                            val compressedFile = compressImageFile(context, fileWithExt)

                            // ✅ Use compressed file, not original
                            val requestFile =
                                compressedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                            val multipart = MultipartBody.Part.createFormData(
                                "File", compressedFile.name, requestFile
                            )

                            // Choose request type
                            if (viewModel.uploadRequestType.value.toString().contains("R")) {
                                viewModel.setRequestType(item?.requestid.toString() ?: "")
                            } else {
                                viewModel.setRequestType(itemMain?.workid.toString() ?: "")
                            }

                            Log.d(
                                "ImageUpload",
                                "Uploading compressed file: ${compressedFile.name} (${compressedFile.length() / 1024} KB)"
                            )

                            // ✅ Upload compressed file
                            viewModel.uploadFile(
                                returnAccessToken(context),
                                SharedPreference.get(context).userId,
                                viewModel.uploadDocType.value,
                                viewModel.uploadRequestType.value,
                                compressedFile
                            )

                            // Optional: Clean up original to save space
                            fileWithExt.delete()
                        }
                    }
                }
            }
        }

    // Permission launcher
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                val photoFile = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "img_${System.currentTimeMillis()}.jpg"
                )
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )
                cameraImageUri.value = uri
                cameraLauncher.launch(uri)
            } else {
                Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    return { callback ->
        onImageSelected = callback
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val photoFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "photo_${System.currentTimeMillis()}.jpg"
            )
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            cameraImageUri.value = uri
            cameraLauncher.launch(uri)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}

@Composable
fun CustomDialogBox() {
    AlertDialog(
        onDismissRequest = { },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.success),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Work Completed",
                    fontFamily = FontFamily(Font(R.font.instrument_sans_bold)),
                    fontSize = 16.sp,
                    color = Color(0xff0E1C21)
                )
            }
        },
        confirmButton = {},
        dismissButton = {})
}

fun getFileNameFromUri(context: Context, uri: Uri): String? {
    val contentResolver = context.contentResolver
    val returnCursor = contentResolver.query(uri, null, null, null, null)
    returnCursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        it.moveToFirst()
        return it.getString(nameIndex) // e.g., "mydoc.pdf"
    }
    return null
}

fun uploadDocx(
    context: Context,
    viewModel: AuthViewModel,
    item: GetWorkListResponse.Data.Wo.WoRequest?,
    bitmap: Bitmap?,
    itemMain: GetWorkListResponse.Data.Wo?
) {
    val imageFile = saveBitmapToFile(context, bitmap)
    imageFile.let { file ->
        val maxFileSizeInBytes = 5 * 1024 * 1024
        val fileSizeInBytes = imageFile.length()
        if (fileSizeInBytes > maxFileSizeInBytes) {
            // File size exceeds 10 MB
            showToastC(
                context,
                "Image resolution is too low. Please upload a higher-quality image."
            )
        } else {
            //  if (viewModel.checkInternetConnection()) {
            val fileWithExtension = File(file.parent, "${file.name}" + ".jpg")
            file.copyTo(fileWithExtension, overwrite = true)
            val requestFile = fileWithExtension.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData(
                "File",
                fileWithExtension.name,
                requestFile
            )

            Log.d("TAGGallery", "rememberImagePicker:$multipartBody----$requestFile")
            //send multipart
            viewModel.setRequestType(itemMain?.workid.toString() ?: "")
            viewModel.uploadDocType.value = CommonUtils.UPLOADDOCUMENTTYPE.SIGNATURE.type.toString()
            viewModel.uploadRequestType.value = "W"
            viewModel.uploadFile(
                returnAccessToken(context),
                SharedPreference.get(context).userId,
                viewModel.uploadDocType.value,
                viewModel.uploadRequestType.value,
                fileWithExtension
            )
        }
    }
}


