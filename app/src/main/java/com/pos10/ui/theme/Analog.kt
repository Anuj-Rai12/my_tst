package com.pos10.ui.theme

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ClockProofScreen() {
    val context = LocalContext.current
    var time1 by remember { mutableStateOf(LocalTime.of(10, 9)) }
    var time2 by remember { mutableStateOf(LocalTime.of(4, 39)) }

    var imageUri1 by remember { mutableStateOf<Uri?>(null) }
    var imageUri2 by remember { mutableStateOf<Uri?>(null) }

    val imagePicker1 = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri1 = it
    }
    val imagePicker2 = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri2 = it
    }

    Column {
        ClockUploadCard(time1, { newTime -> time1 = newTime }, imageUri1) {
            imagePicker1.launch("image/*")
        }
        ClockUploadCard(time2, { newTime -> time2 = newTime }, imageUri2) {
            imagePicker2.launch("image/*")
        }

        Button(
            onClick = { /* Proceed */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Next")
        }
    }
}


@Composable
fun ClockUploadCard(
    time: LocalTime,
    onTimeChange: (LocalTime) -> Unit,
    imageUri: Uri?,
    onImagePick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color = Color(0xFFF9F9F9))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnalogClock(time)
            Spacer(Modifier.height(4.dp))
            Text(time.format(DateTimeFormatter.ofPattern("HH:mm")))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .clickable { onImagePick() },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Selected Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    //Icon(Icons.Default.CloudUpload, contentDescription = null)
                    //Text("Upload")
                }
            }
        }
    }
}

@Composable
fun AnalogClock(time: LocalTime, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(100.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2.2f

        // Draw clock circle
        drawCircle(Color.LightGray, radius, center)

        // Draw hour and minute hands
        val hourAngle = ((time.hour % 12) + time.minute / 60f) * 30f
        val minuteAngle = time.minute * 6f

        val hourHandLength = radius * 0.5f
        val minuteHandLength = radius * 0.8f

        fun angleToOffset(angle: Float, length: Float): Offset {
            val rad = Math.toRadians(angle - 90.0)
            return Offset(
                center.x + (length * cos(rad)).toFloat(),
                center.y + (length * sin(rad)).toFloat()
            )
        }

        drawLine(
            Color.Black,
            center,
            angleToOffset(hourAngle, hourHandLength),
            strokeWidth = 6f
        )
        drawLine(
            Color.DarkGray,
            center,
            angleToOffset(minuteAngle, minuteHandLength),
            strokeWidth = 4f
        )
    }
}

