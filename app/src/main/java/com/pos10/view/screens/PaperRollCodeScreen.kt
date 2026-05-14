package com.pos10.view.screens

import android.os.CountDownTimer
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.pos10.R
import com.pos10.domain.AuthViewModel
import com.pos10.helper.CommonUtils
import com.pos10.helper.CommonUtils.convertDateTimeFormat
import com.pos10.helper.CommonUtils.convertDateTimeToDate
import com.pos10.helper.CommonUtils.maskEmail
import com.pos10.helper.CommonUtils.returnAccessToken
import com.pos10.helper.CommonUtils.showToastC
import com.pos10.helper.CustomLoader
import com.pos10.helper.EmpResource
import com.pos10.helper.ErrorUtil
import com.pos10.helper.SharedPreference
import com.pos10.model.local.GenerateOTPRequest
import com.pos10.model.local.UpdateWorkOrderCompletedRequest
import com.pos10.model.remote.GetWorkListResponse
import com.pos10.model.remote.ValidateOTPRequest
import com.pos10.view.MainActivity
import com.pos10.view.SelectedRequestHolder
import com.pos10.view.SingletonObject
import com.pos10.view.navigation.Screen
import kotlinx.coroutines.delay

@Composable
fun PaperRollConfirmationScreen(navHostController: NavHostController,
                                viewModel: AuthViewModel = hiltViewModel()) {
    val context = LocalContext.current
    (context as MainActivity).visibleStatusBar(context)
    val lifecycleOwner = LocalLifecycleOwner.current

    var code by remember { mutableStateOf("") }
    val item = SelectedRequestHolder.selectedSUbWorkItemList
    val itemMain = SelectedRequestHolder.selectedItemList
    var focous= LocalFocusManager.current

    LaunchedEffect(Unit) {
        val model = GenerateOTPRequest(itemMain?.merchantName?.trim()?:"", 14901,
            itemMain?.email?:"abc@gmail.com",itemMain?.workid.toString())
        viewModel.hitGetGenerateOTP(returnAccessToken(context), model)
        observerUpdatePaperRollUpdate(context, viewModel, navHostController, lifecycleOwner)
    }

    LaunchedEffect(Unit) {
        viewModel.updateWorkOrderCompleted.collect { status ->
            Log.d("TAG", "InstallationDeviceDetails: Status ---${status} ")
            when (status) {
                CommonUtils.STATUS.INPROGRESS.sttausname -> {

                }

                CommonUtils.STATUS.FAILED.sttausname -> {

                }

                CommonUtils.STATUS.COMPLETED.sttausname -> {
                    if (SingletonObject.fromWhere.contains("Complaint")) {
                        showToastC(context, "Complaint Resolved")
                        navHostController.navigate(Screen.DashboardScreen.route) {
                            popUpTo(0) {
                                inclusive = true
                            }
                        }
                    } else {
                        viewModel.showDialog = true
                    }
                }
            }
        }
    }

    BackHandler {
        navHostController.navigate(Screen.DashboardScreen.route) {
            popUpTo(0) {
                inclusive = true
            }
        }
    }

    if (viewModel.showDialog) {
        CustomDialogBox()
        LaunchedEffect(Unit) {
            delay(3000)
            viewModel.showDialog = false
            navHostController.navigate(Screen.DashboardScreen.route) {
                popUpTo(0) {
                    inclusive = true
                }
            }
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(color = Color.White)) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.White)
            .padding(top = 40.dp, bottom = 10.dp)
            .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.ic_back_black),
                contentDescription = "",
                modifier = Modifier
                    .width(16.dp)
                    .clickable {
                        navHostController.navigate(Screen.DashboardScreen.route) {
                            popUpTo(0) {
                                inclusive = true
                            }
                        }
                    },
            )
            Spacer(Modifier.width(5.dp))
            Text("Job Confirmation",
                color = Color.Black,
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)))
        }

        // Scrollable content
        Column(modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(8.dp))
            Text("Get Confirmation",
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 16.dp),
                fontFamily = FontFamily(Font(R.font.instrument_sans_bold)),
                color = Color(0XFF000000))

            Spacer(Modifier.height(8.dp))
            val email = itemMain?.email?:"abc@gmail.com"
            val maskedEmail = maskEmail(email)

            Text("OTP has been sent to the merchant's registered email $maskedEmail",
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
                color = Color.Gray)

            Spacer(Modifier.height(4.dp))
            TextField(value = code,
                onValueChange = { code = it.filter { char -> char.isDigit() }.take(6) },
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                    .fillMaxWidth()
                    .height(55.dp)
                    .border(width = 1.dp,
                        color = Color(0xFFF6F6F6),
                        shape = RoundedCornerShape(8.dp))
                    .background(color = Color.White, shape = RoundedCornerShape(8.dp)),
                placeholder = {
                    Text(text = "Enter 6-digit code from merchant", color = Color.Gray)
                },
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = Color.Black,
                    focusedTextColor = Color.Black,
                    unfocusedPlaceholderColor = Color.Gray,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent))
            Row(modifier = Modifier.padding(start = 16.dp)) {
                ClickableTextExample(context,viewModel,itemMain) {
                    if (it) {
                        code=""
                        focous.clearFocus(true)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp)
                .height(45.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color = Color.Black)
                .clickable {
                    if (code.isEmpty() || code.length < 6) {
                        showToastC(context, "Please enter a valid 6-digit code")
                        return@clickable
                    }
                    val model = ValidateOTPRequest(
                        itemMain?.merchantName?.trim()?:"",
                        code.toInt(),
                        14901,
                    )
                    viewModel.hitValidateOTP(returnAccessToken(context), model)
                }, contentAlignment = Alignment.Center) {
                Text(text = "Confirm Delivery",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }
        }
    }
}


@Composable
fun ClickableTextExample(context: MainActivity,
                         viewModel: AuthViewModel,
                         itemMain:GetWorkListResponse.Data.Wo?,
                         onResent: (Boolean) -> Unit) {
    Box(contentAlignment = Alignment.CenterStart, // This aligns the Text inside the Box
        modifier = Modifier.fillMaxWidth() // Ensures the Box takes full width if needed
    ) {
        var timeLeft by remember { mutableStateOf(30) }
        var startTimer by remember { mutableStateOf(true) }
        var resend by remember { mutableStateOf("") }
        var color by remember { mutableStateOf(Color(0XFF848484)) }

        Text(text = resend,
            fontSize = 12.sp,
            color = color,
            fontFamily = FontFamily(Font(R.font.instrument_sans_regular)),
            modifier = Modifier.clickable {
                if (resend.toString().contains("Resend")) {
                    startTimer = true
                    onResent(true)
                    val model = GenerateOTPRequest(itemMain?.merchantName?.trim()?:"",
                        14901,
                        itemMain?.email?:"abc@gmail.com",
                        itemMain?.workid.toString())
                    viewModel.hitGetGenerateOTP(returnAccessToken(context), model)

                } else {
                    startTimer = false
                } // Only clickable, no align needed here
            })

        if (startTimer) {
            LaunchedEffect(key1 = Unit) {
                val countDownTimer = object : CountDownTimer(90000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        timeLeft = (millisUntilFinished / 1000).toInt()
                        color = Color(0XFF848484)
                        resend = "Resend OTP in $timeLeft seconds"
                    }

                    override fun onFinish() {
                        timeLeft = 0
                        startTimer = false
                    }
                }
                countDownTimer.start()

                // Delay for 1 second after countdown finishes to show 0
                delay(1000)

            }
        } else {
            color = Color(0XFF33BD8C)
            resend = "Resend OTP"
        }
    }
}

fun observerUpdatePaperRollUpdate(
    context: MainActivity,
    viewModel: AuthViewModel,
    navHostController: NavHostController?,
    lifecycleOwner: LifecycleOwner,
) {

    viewModel.getGenerateOtpLiveData.observe(lifecycleOwner) {
        when (it) {
            is EmpResource.Failure -> {
                it.throwable?.let { it1 -> ErrorUtil.handlerGeneralError(context, it1) }
                CustomLoader.hideLoader()
            }

            EmpResource.Loading -> {
                CustomLoader.showLoader(context)
            }

            is EmpResource.Success -> {
                CustomLoader.hideLoader()
               // viewModel.emailId.value= it.value.data.generatedOTP.toString()
                showToastC(context, it.value.info.message.toString())
            }
        }
    }

    viewModel.validateOtpLiveData.observe(lifecycleOwner) {
        when (it) {
            is EmpResource.Failure -> {
                it.throwable?.let { it1 -> ErrorUtil.handlerGeneralError(context, it1) }
                CustomLoader.hideLoader()
            }

            EmpResource.Loading -> {
                CustomLoader.showLoader(context)
            }

            is EmpResource.Success -> {
                CustomLoader.hideLoader()
                if (it.value.info.isSuccess == false) {
                    showToastC(context, it.value.info.message.toString())
                    return@observe
                }

                val item = SelectedRequestHolder.selectedSUbWorkItemList
                val itemMain = SelectedRequestHolder.selectedItemList
                val appointment = itemMain?.appointmentDate
                val finalDate =
                    if (!appointment.isNullOrEmpty() && Regex("\\d{4}-\\d{2}-\\d{2}").matches(
                            appointment)) {
                        // Already in yyyy-MM-dd format, use as is
                        appointment
                    } else {
                        // Not in yyyy-MM-dd format → convert
                        val dattee = convertDateTimeToDate(appointment ?: "18-09-2025 00:00:00")
                        convertDateTimeFormat(dattee)
                    }

                val model =UpdateWorkOrderCompletedRequest(itemMain?.workid.toString(),
                    CommonUtils.STATUS.COMPLETED.type,SharedPreference.get(context).userId.toInt(),
                    "","","")

                viewModel.hitUpdateWorkOrderCompleted(returnAccessToken(context),model,CommonUtils.STATUS.COMPLETED.sttausname)
            }
        }
    }

}
