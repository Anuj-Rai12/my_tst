package com.pos10.view.screens.qrscanner

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

fun generateQRCodeBitmap(text: String, size: Int = 512): Bitmap {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
        }
    }
    return bitmap
}

fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Uri? {
    val filename = "scanned_qr_${System.currentTimeMillis()}.png"
    val resolver = context.contentResolver

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
    }

    val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    imageUri?.let {
        resolver.openOutputStream(it).use { stream ->
            if (stream != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
        }
    }
    return imageUri
}

@Composable
fun QRImageDisplay(imageUri: Uri?) {
    if (imageUri != null) {
        Image(
            painter = rememberAsyncImagePainter(imageUri),
            contentDescription = "Scanned QR Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(16.dp)
        )
    }
}


