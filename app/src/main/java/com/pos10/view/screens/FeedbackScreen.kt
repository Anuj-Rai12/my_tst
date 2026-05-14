package com.pos10.view.screens

import android.os.Build
import android.util.Log
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.navigation.NavHostController
import com.pos10.R
import com.pos10.domain.AuthViewModel
import com.pos10.helper.CommonUtils.formatDate
import com.pos10.helper.CommonUtils.returnAccessToken
import com.pos10.helper.CustomLoader
import com.pos10.helper.EmpResource
import com.pos10.helper.ErrorUtil
import com.pos10.helper.SharedPreference
import com.pos10.model.local.GetAgentFeedbackRequest
import com.pos10.model.remote.GetFeedbackResponse
import com.pos10.view.MainActivity

@Composable
fun FeedbackScreen(
    navHostController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = context as? MainActivity  // Safe cast to avoid ClassCastException

    // Apply window settings when this Composable first launches
    LaunchedEffect(Unit) {
        activity?.visibleStatusBar(context)
        activity?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                WindowCompat.getInsetsController(window, window.decorView)
                    ?.isAppearanceLightStatusBars = true
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility or
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

                @Suppress("DEPRECATION")
                window.statusBarColor = Color.Transparent.toArgb()
            }
        }

        // Initialize feedback request
        val model = GetAgentFeedbackRequest(
            SharedPreference.get(context).userId.toInt()
        )
        viewModel.hitFeedbackRequest(returnAccessToken(context), model)
    }

    // Observe LiveData in a lifecycle-safe way
    DisposableEffect(lifecycleOwner) {
        val observer = Observer<EmpResource<GetFeedbackResponse>> { result ->
            when (result) {
                is EmpResource.Failure -> {
                    result.throwable?.let {
                        ErrorUtil.handlerGeneralError(context, it)
                    }
                    CustomLoader.hideLoader()
                }
                EmpResource.Loading -> {
                    CustomLoader.showLoader(context as MainActivity)
                }
                is EmpResource.Success -> {
                    CustomLoader.hideLoader()
                    viewModel.feedbackData = result.value.data ?: arrayListOf()
                }
            }
        }
        viewModel.fieldFeedbackLiveData.observe(lifecycleOwner, observer)

        onDispose {
            viewModel.fieldFeedbackLiveData.removeObserver(observer)
        }
    }

    Log.d("TAG", "FeedbackScreen: Screen  1   --${viewModel.feedbackData}")

    // UI
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = 40.dp, bottom = 10.dp)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_back_black),
                    contentDescription = "",
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { navHostController.popBackStack() },
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "My Rating",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold))
                )
            }

            // Ratings List
            if (viewModel.feedbackData.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White),
                    contentPadding = PaddingValues(vertical = 0.dp)
                ) {
                    Log.d("TAG", "FeedbackScreen: Screen    --${viewModel.feedbackData}")
                    items(viewModel.feedbackData.size) { index ->
                        val item = viewModel.feedbackData[index]
                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.logout_ic),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            text = item.merchantName,
                                            color = Color.Black,
                                            fontSize = 14.sp,
                                            fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)))

                                        if(item.createdDate.isNotEmpty()) {
                                            val formattedDate = formatDate(item.createdDate)
                                            Text(text = "$formattedDate",
                                                color = Color.Gray,
                                                fontSize = 12.sp,
                                                fontFamily = FontFamily(Font(R.font.instrument_sans_regular)))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row {
                                        val ratingValue = item.rating.toDoubleOrNull() ?: 0.0
                                        for (i in 1..5) {
                                            Image(
                                                painter = painterResource(
                                                    id = if (i <= ratingValue)
                                                        R.drawable.rating_star_yellow_fill
                                                    else
                                                        R.drawable.rating_star_grey
                                                ),
                                                contentDescription = "rating star",
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.remarks,
                                        color = Color.Black,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // No Ratings State
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Ratings",
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)),
                        color = Color.Black
                    )
                }
            }
        }
    }
}

fun observerFeedback(context: MainActivity,
                     lifecycleOwner: LifecycleOwner,
                     navHostController: NavHostController,
                     viewModel: AuthViewModel) {

    viewModel.fieldFeedbackLiveData.observe(lifecycleOwner) {
        when (it) {
            is EmpResource.Failure -> {
                it.throwable?.let { it1 -> ErrorUtil.handlerGeneralError(context, it1) }
                CustomLoader.hideLoader()
            }

            EmpResource.Loading -> {
                CustomLoader.showLoader(context)
            }
            is EmpResource.Success-> {
                CustomLoader.hideLoader()
                viewModel.feedbackData= it.value.data
            }
        }
    }
}
