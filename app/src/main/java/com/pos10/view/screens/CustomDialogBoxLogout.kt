package com.pos10.view.screens

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import com.pos10.R
import com.pos10.domain.AuthViewModel

@Composable
fun CustomDialogBoxLogout(context: Activity,
                          viewModel: AuthViewModel,
                          lifecycleOwner: LifecycleOwner,
                          navHostController: NavHostController,
                          onResult: (String) -> Unit) {

    Dialog(onDismissRequest = { onResult("cancel") }) {
        Surface(shape = RoundedCornerShape(12.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)) {

            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painter = painterResource(id = R.drawable.ic_logout),
                    contentDescription = null,
                    modifier = Modifier.size(66.dp))

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Are you sure you want to logout?",
                    fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color(0xff0E1C21))

                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    // Yes Button
                    Box(modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFFff6900), RoundedCornerShape(26.dp))
                        .background(Color.White, RoundedCornerShape(26.dp))
                        .clickable { onResult("yes") }
                        .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center) {
                        Text(text = "Yes",
                            fontSize = 16.sp,
                            color = Color(0xFFff6900),
                            fontFamily = FontFamily(Font(R.font.instrument_sans_medium)))
                    }

                    // No Button
                    Box(modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFF6F6F6), RoundedCornerShape(26.dp))
                        .clickable { onResult("no") }
                        .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center) {
                        Text(text = "No",
                            fontSize = 16.sp,
                            color = Color.Black,
                            fontFamily = FontFamily(Font(R.font.instrument_sans_medium)))
                    }
                }
            }
        }
    }
}
