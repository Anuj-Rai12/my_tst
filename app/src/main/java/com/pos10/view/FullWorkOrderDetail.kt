package com.pos10.view

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.pos10.view.screens.InfoRow
import com.pos10.view.screens.downloadFile

@Composable
fun FullWorkOrderDetail(navHostController: NavHostController, viewModel: AuthViewModel = hiltViewModel()) {
    val context = LocalContext.current
    (context as MainActivity).visibleStatusBar(context)
    BackHandler { navHostController.popBackStack() }
    val itemMain  = SelectedRequestHolder.selectedItemList
    LaunchedEffect(Unit) {
        viewModel.setRequestId(itemMain?.workid.toString())
        viewModel.setRequestTypeee("W")
    }
    val downloadFiles by viewModel.allFiles.collectAsState()
    val downloadList = downloadFiles.filter { item ->
        item.filePath.contains("", ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(bottom = 20.dp)
            .background(color = Color.White)) {
            Row(modifier = Modifier.fillMaxWidth().background(color = Color.White)
                .padding(top = 40.dp, bottom = 10.dp).padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically) {

                Image(painter = painterResource(R.drawable.ic_back_black),
                    contentDescription = "",
                    modifier = Modifier.width(16.dp).clickable {
                        navHostController.popBackStack()
                    }, )
                Spacer(Modifier.width(8.dp))
                Text("Merchant Id:${itemMain?.merchantCode}",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)))
            }

            // Scrollable content
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        Log.d("TAG", "InstallationDeviceDetailsdada:${viewModel.simno.value} ")
                        InfoRow(label = "Merchant Name:", value = itemMain?.merchantName ?: "N/A")
                        InfoRow(label = "W.Order No:", value = itemMain?.workOrderNo ?: "N/A")
                    }
                }

                if (downloadList.isNotEmpty()) {
                        Text(text = "Download Files",
                            fontSize = 16.sp,
                            color = Color.Black,
                            fontFamily = FontFamily(Font(R.font.instrument_sans_medium)),
                            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 8.dp))

                        val downDistinctFiles =
                            downloadList.groupBy { it.originalFileName }.mapNotNull { (_, files) ->
                                    // Pick the one that contains the full URL (http/https)
                                    files.firstOrNull { it.filePath.startsWith("http") }
                                }

                        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
                            .padding(horizontal = 16.dp)) {
                            items(downDistinctFiles) { fileItem ->
                                val fileName = fileItem.originalFileName ?: "File"
                                val fileUrl = fileItem.filePath ?: ""
                                val uploadType = fileItem.uploadType ?: ""

                                Text(text = "$uploadType:",
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)),
                                    color = Color.Black,
                                    modifier = Modifier.weight(1f))
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                        .clickable {
                                            downloadFile(context, fileUrl, fileName)
                                        }) {
                                    // Thumbnail
                                    if (fileName.endsWith(".pdf", true) || fileName.endsWith(".xls",
                                            true) || fileName.endsWith(".xlsx", true)) {
                                        Image(painter = painterResource(id = R.drawable.pdf),
                                            contentDescription = null,
                                            modifier = Modifier.size(40.dp)
                                                .clip(RoundedCornerShape(6.dp)))
                                    } else {
                                        AsyncImage(model = fileUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.size(40.dp)
                                                .clip(RoundedCornerShape(8.dp)).border(1.dp,
                                                    Color(0xFFF6F6F6),
                                                    RoundedCornerShape(8.dp)))
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))
                                    // File name
                                    Text(text = fileName,
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
                                        color = Color.Black,
                                        modifier = Modifier.weight(1f))

                                    Image(painter = painterResource(R.drawable.ic_download, ),
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 8.dp).size(24.dp)
                                            .clickable {
                                                downloadFile(context, fileUrl, fileName)
                                            })
                                }

                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

            }
        }
    }
}