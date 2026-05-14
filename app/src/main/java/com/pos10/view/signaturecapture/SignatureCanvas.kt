package com.pos10.view.signaturecapture

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun SignatureCanvas(
    modifier: Modifier = Modifier,
    strokeWidth: Float = 4f,
    strokeColor: Color = Color.Black,
    pathState: MutableState<Path>
) {
    val lastPoint = remember { mutableStateOf<Offset?>(null) }

    Box(
        modifier = modifier
            .background(Color.White)
            .border(1.dp, Color.Gray)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        pathState.value.moveTo(offset.x, offset.y)
                        lastPoint.value = offset
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newPoint = lastPoint.value?.plus(dragAmount) ?: return@detectDragGestures
                        pathState.value.lineTo(newPoint.x, newPoint.y)
                        lastPoint.value = newPoint
                        // Force recomposition by updating state
                        pathState.value = Path().apply {
                            this.addPath(pathState.value)
                        }
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawPath(
                path = pathState.value,
                color = strokeColor,
                style = Stroke(width = strokeWidth)
            )
        }
    }
}
