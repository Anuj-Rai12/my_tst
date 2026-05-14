package com.pos10.view.signaturecapture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.io.File
import java.io.FileOutputStream

@Composable
fun SignatureDialog(
    onDismiss: () -> Unit,
    onSignatureCaptured: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val capturedPath = remember { mutableStateOf(Path()) }

    val canvasWidth = 400
    val canvasHeight = 550

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier
                .width(400.dp)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Signature",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                SignatureCanvas(
                    modifier = Modifier
                        .width(canvasWidth.dp)
                        .height(canvasHeight.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    pathState = capturedPath)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = {
                        capturedPath.value.reset()
                        capturedPath.value = Path()
                    }) {
                        Text("Clear")
                    }

                    Row {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            val bitmap = saveSignatureToBitmap(
                                context = context,
                                path = capturedPath.value,
                                widthDp = canvasWidth,
                                heightDp = canvasHeight)
                            onSignatureCaptured(bitmap)
                            onDismiss()
                        }) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

fun saveSignatureToBitmap(
    context: Context,
    path: Path,
    widthDp: Int,
    heightDp: Int,
    strokeWidth: Float = 4f,
    strokeColor: Color = Color.Black
): Bitmap {
    val density = context.resources.displayMetrics.density
    val widthPx = (widthDp * density).toInt()
    val heightPx = (heightDp * density).toInt()

    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)

    val paint = Paint().apply {
        color = strokeColor.toArgb()
        style = Paint.Style.STROKE
        isAntiAlias = true
        this.strokeWidth = strokeWidth * density // scale stroke too
    }

    canvas.drawPath(path.asAndroidPath(), paint)
    return bitmap
}

fun saveBitmapToFile(context: Context, bitmap: Bitmap?): File {
    val file = File(context.cacheDir, "signature_${System.currentTimeMillis()}")
    FileOutputStream(file).use { out ->
        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    return file
}



