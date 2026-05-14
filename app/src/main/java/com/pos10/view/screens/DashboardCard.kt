package com.pos10.view.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pos10.R


@Composable
fun DashboardCard(
    title: String,
    count: Int,
    iconRes: Int,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null) {
    val backgroundColor = if (isSelected) Color(0x33ff6900) else Color.Transparent
    val iconBgColor = if (isSelected) Color(0xFFffe2cc) else Color(0xFFF6F6F6)

    Column(
        modifier = modifier
//            .background(backgroundColor, shape = RoundedCornerShape(8.dp))
            .padding(vertical = 10.dp)
            .clickable { onClick?.invoke() }
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(color = Color(0xFFF6F6F6))) {
            Image(painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(28.dp))
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(text = "$count",
            fontSize = 12.sp,
            fontFamily = FontFamily(Font(R.font.instrument_sans_medium)),
            color = Color(0xFFff6900))

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = title,
            fontSize = 12.sp,
            fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
            color = if (isSelected) Color(0xFFff6900) else Color.Black
        )
    }
}