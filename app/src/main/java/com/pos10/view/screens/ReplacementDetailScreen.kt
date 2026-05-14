package com.pos10.view.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.pos10.R
import com.pos10.domain.AuthViewModel
import com.pos10.helper.CommonUtils
import com.pos10.helper.CommonUtils.convertDateTimeFormat
import com.pos10.helper.CommonUtils.convertDateTimeToDate
import com.pos10.helper.CommonUtils.returnAccessToken
import com.pos10.helper.SharedPreference
import com.pos10.model.local.UpdateRequest
import com.pos10.view.MainActivity
import com.pos10.view.SelectedRequestHolder
import com.pos10.view.SingletonObject
import com.pos10.view.navigation.Screen

@Composable
fun ReplacementDetailScreen(navHostController: NavHostController, viewModel: AuthViewModel = hiltViewModel()) {
    val context = LocalContext.current
    (context as MainActivity).visibleStatusBar(context)

    val item = SelectedRequestHolder.selectedSUbWorkItemList
    val itemMain = SelectedRequestHolder.selectedItemList

    LaunchedEffect(Unit) {
        viewModel.updateSuccess.collect { status ->
            SingletonObject.fromWhere = "Replacement"
            when (status) {
                CommonUtils.STATUS.INPROGRESS.sttausname -> {
                    navHostController.navigate(Screen.UploadDocumentsScreen.route)
                }
                CommonUtils.STATUS.FAILED.sttausname -> {
                }
            }
        }
    }

    BackHandler {
        navHostController.popBackStack()
    }

    LaunchedEffect(Unit) {
        viewModel.setRequestId(item?.requestid.toString())
        viewModel.setRequestTypeee("R")
    }
    val downloadFiles by viewModel.allFiles.collectAsState()
    val downloadList = downloadFiles.filter { it.filePath.isNotBlank() }

    val appointment = itemMain?.appointmentDate
    val finalDate = if (!appointment.isNullOrEmpty() && Regex("\\d{4}-\\d{2}-\\d{2}").matches(appointment)) {
        appointment
    } else {
        val dattee = convertDateTimeToDate(appointment ?: "18-09-2025 00:00:00")
        convertDateTimeFormat(dattee)
    }

    Column(modifier = Modifier.fillMaxSize().background(color = Color.White)) {
        Row(modifier = Modifier.fillMaxWidth().background(color = Color.White).padding(top = 40.dp, bottom = 10.dp).padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(R.drawable.ic_back_black), contentDescription = "", modifier = Modifier.width(16.dp).clickable { navHostController.popBackStack() })
            Spacer(Modifier.width(5.dp))
            Text("Replacement", color = Color.Black, fontSize = 18.sp, fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)))
        }

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), elevation = CardDefaults.cardElevation(4.dp), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(6.dp)) {
                    InfoRow(label = "Serial No:", value = item?.serialNo ?: "N/A")
                    InfoRow(label = "Device Type:", value = item?.deviceType?.takeIf { it.isNotBlank() } ?: "N/A")
                    InfoRow(label = "Merchant Name:", value = item?.merchantName ?: "N/A")
                    InfoRow(label = "Device ID:", value = item?.deviceNo?.takeIf { it.isNotBlank() } ?: "N/A")
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Replacement Items", fontSize = 14.sp, modifier = Modifier.padding(start = 16.dp), fontFamily = FontFamily(Font(R.font.instrument_sans_bold)), color = Color.Black)
            
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp).background(color = Color(0xFFeeeee4), shape = RoundedCornerShape(8.dp)).padding(8.dp)) {
                Text("Replacement Request", fontSize = 12.sp, fontFamily = FontFamily(Font(R.font.instrument_sans_medium)), color = Color.Black)
                Text("Quantity: ${item?.quantity.toString()} units", fontSize = 12.sp, fontFamily = FontFamily(Font(R.font.instrument_sans_regular)), color = Color.Black)
            }

            if (item?.status.toString().equals(CommonUtils.STATUS.COMPLETED.sttausname)) {
                if (downloadList.isNotEmpty()) {
                    Text(text = "Download Files", fontSize = 16.sp, color = Color.Black, fontFamily = FontFamily(Font(R.font.instrument_sans_medium)), modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp))
                    val downDistinctFiles = downloadList.groupBy { it.originalFileName }.mapNotNull { (_, files) -> files.firstOrNull { it.filePath.startsWith("http") } }
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp).padding(horizontal = 16.dp)) {
                        items(downDistinctFiles) { fileItem ->
                            val fileName = fileItem.originalFileName ?: "File"
                            val fileUrl = fileItem.filePath
                            val uploadType = fileItem.uploadType ?: ""
                            Text(text = "$uploadType:", fontSize = 14.sp, fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)), color = Color.Black)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { downloadFile(context, fileUrl, fileName) }) {
                                if (fileName.endsWith(".pdf", true)) {
                                    Image(painter = painterResource(id = R.drawable.pdf), contentDescription = null, modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp)))
                                } else {
                                    AsyncImage(model = fileUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFF6F6F6), RoundedCornerShape(8.dp)))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = fileName, fontSize = 14.sp, fontFamily = FontFamily(Font(R.font.instrument_sans_regular)), color = Color.Black, modifier = Modifier.weight(1f))
                                Image(painter = painterResource(R.drawable.ic_download), contentDescription = null, modifier = Modifier.padding(end = 8.dp).size(24.dp).clickable { downloadFile(context, fileUrl, fileName) })
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp).height(45.dp).clip(RoundedCornerShape(8.dp)).background(color = Color.Black).clickable {
                    val model = UpdateRequest("", "1900-01-01", false, "", SharedPreference.get(context).userId.toInt(), 0, 0, item?.requestid?.toInt() ?: 0, CommonUtils.STATUS.INPROGRESS.type, 0, finalDate, itemMain?.appointmentTime ?: "04:06 pm")
                    viewModel.hitUpdateRequest(model, CommonUtils.STATUS.INPROGRESS.sttausname)
                }, contentAlignment = Alignment.Center) {
                    Text(text = "Proceed to Replacement", color = Color.White, fontSize = 14.sp, fontFamily = FontFamily(Font(R.font.instrument_sans_regular)))
                }
            }
        }
    }
}
