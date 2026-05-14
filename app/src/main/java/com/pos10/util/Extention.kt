package com.pos10.util

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import android.os.Build
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream


@Composable
fun SetStatusBar(color: Color, isLightIcons: Boolean) {
    val context = LocalContext.current
    val activity = context.findActivity() ?: return
    val window = activity.window
    val view = LocalView.current
    window.statusBarColor = color.toArgb()
    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isLightIcons
}


fun ComponentActivity.transparentStatusBar(activity: ComponentActivity) {
    WindowCompat.setDecorFitsSystemWindows(activity.window, false)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        activity.window.setDecorFitsSystemWindows(false)
        activity.window.statusBarColor = Color.Transparent.toArgb()
        activity.window.navigationBarColor = Color.Transparent.toArgb()
    } else {
        @Suppress("DEPRECATION")
        activity.window.decorView.systemUiVisibility =
            activity.window.decorView.systemUiVisibility or
                    android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        @Suppress("DEPRECATION")
        activity.window.statusBarColor = Color.Transparent.toArgb()
    }
}


fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

}

fun uriToFile(context: Context, uri: Uri): File? {
    val contentResolver = context.contentResolver ?: return null
    var time = System.currentTimeMillis()
    val tempFile = File(context.cacheDir, "img_$time")
    contentResolver.openInputStream(uri)?.use { inputStream ->
        FileOutputStream(tempFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }
    return tempFile
}

fun convertFileToMultipartBody(partName: String, file: File): MultipartBody.Part {
    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(partName, file.name, requestFile)
}

fun isValidPassword(password: String): Boolean {
    val passwordRegex = Regex("^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*()_+=\\[\\]{}|;:'\",.<>?/-]).{6,}\$")
    return passwordRegex.matches(password)
}

fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.(com|in)$"
    return Regex(emailRegex).matches(email)
}

//fun isValidEmail(email: String): Boolean {
//    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.(com|in)$"
//    return Regex(emailRegex).matches(email)
//}
fun saveBitmapToUri(context: Context, bitmap: Bitmap): Uri? {
    val imagesDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyImages")
    if (!imagesDir.exists()) imagesDir.mkdirs()

    val file = File(imagesDir, "profile_${System.currentTimeMillis()}.png")
    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    color: Color = Color.LightGray,
    thickness: Dp = 1.dp,
    height: Dp = 24.dp
) {
    Box(
        modifier
            .width(thickness)
            .height(height)
            .background(color = color)
    )
}






