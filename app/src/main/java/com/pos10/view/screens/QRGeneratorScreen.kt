//package com.pay10.view.screens
//
//import android.graphics.Bitmap
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.asImageBitmap
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import com.google.zxing.BarcodeFormat
//import com.google.zxing.MultiFormatWriter
//import com.google.zxing.common.BitMatrix
//
//@Composable
//fun QRGeneratorScreen() {
//    var text by remember { mutableStateOf("") }
//    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        OutlinedTextField(
//            value = text,
//            onValueChange = { text = it },
//            label = { Text("Enter text") },
//            modifier = Modifier.fillMaxWidth()
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Button(onClick = {
//            qrBitmap = generateQRCode(text)
//        }) {
//            Text("Generate QR Code")
//        }
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        qrBitmap?.let {
//            Image(
//                bitmap = it.asImageBitmap(),
//                contentDescription = "QR Code",
//                modifier = Modifier.size(250.dp))
//        }
//    }
//}
//
//
//fun generateQRCode(content: String, size: Int = 512): Bitmap? {
//    return try {
//        val bitMatrix: BitMatrix =
//            MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
//        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
//
//        for (x in 0 until size) {
//            for (y in 0 until size) {
//                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
//            }
//        }
//        bitmap
//    } catch (e: Exception) {
//        null
//    }
//}
//
