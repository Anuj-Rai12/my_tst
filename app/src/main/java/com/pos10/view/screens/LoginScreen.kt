package com.pos10.view.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import com.google.firebase.messaging.FirebaseMessaging
import com.pos10.R
import com.pos10.domain.AuthViewModel
import com.pos10.helper.BiometricHelper
import com.pos10.helper.CommonUtils.returnAccessToken
import com.pos10.helper.CommonUtils.showToastC
import com.pos10.helper.CustomLoader
import com.pos10.helper.EmpResource
import com.pos10.helper.ErrorUtil
import com.pos10.helper.SharedPreference
import com.pos10.model.local.AuthRequest
import com.pos10.model.local.FcmRequest
import com.pos10.view.MainActivity
import com.pos10.view.ObjectForTab
import com.pos10.view.SingletonObject
import com.pos10.view.reuseable_component.ProjectButton
import com.pos10.view.navigation.Screen

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController? = null,
    viewModel: AuthViewModel = hiltViewModel()) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    (context as MainActivity).visibleStatusBar(context)
    val lifecycleOwner = LocalLifecycleOwner.current

    /**************
     * Fetch FCM Token
     */
    FirebaseMessaging.getInstance().token
        .addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM_TOKEN", "Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("FCM_TOKEN", "Manual fetch: $token")
            SharedPreference.get(context).fcmToken= token
            // Send token to server if needed
        }

    /**********
     * BIOMETRIC
     */

    val biometricLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        val resultStr = data?.getStringExtra(BiometricHelper.EXTRA_RESULT)

        if (result.resultCode == Activity.RESULT_OK &&
            resultStr == BiometricHelper.RESULT_SUCCESS) {
            showToastC(context,"Success")
            Log.d("TAG", "LoginScreen:success $resultStr---$data")

        } else {
            showToastC(context,"Failed")
            Log.d("TAG", "LoginScreen:failed ")
        }
    }

    LaunchedEffect(Unit) {
        observerLogin(context, viewModel, navController, lifecycleOwner)
    }

    BackHandler {
        (context as? Activity)?.finishAffinity()
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)
        .padding(horizontal = 20.dp)
        .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Centered Header (logo + titles)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(painter = painterResource(id = R.drawable.logo_pos10),
                    contentDescription = null,
                    modifier = Modifier.size(110.dp))

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Field Management Service",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFff6900),
                    fontFamily = FontFamily(Font(R.font.instrument_sans_semi_bold)))

                Text(
                    text = "Engineer's Portal",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF7F9CA6),
                    fontFamily = FontFamily(Font(R.font.instrument_sans_regular))
                )
            }

            // Left-aligned Login Form
            Column(modifier = Modifier.fillMaxWidth()) {
                // Username
                Text(
                    stringResource(R.string.username),
                    fontSize = 14.sp,
                    fontWeight = FontWeight(400),
                    color = Color(0XFF35494F),
                    fontFamily = FontFamily(Font(R.font.instrument_sans_regular)))

                Spacer(Modifier.height(8.dp))

                var textFieldValue by remember {
                    mutableStateOf(TextFieldValue(text = email, selection = TextRange(email.length)))
                }

                LaunchedEffect(textFieldValue.text) {
                    if (textFieldValue.text.startsWith(" ")) {
                        val trimmed = textFieldValue.text.trimStart()
                        textFieldValue = textFieldValue.copy(text = trimmed, selection = TextRange(trimmed.length))
                        email = trimmed
                    } else if (textFieldValue.text != email) {
                        email = textFieldValue.text
                    }
                }

                TextField(value = textFieldValue,
                            onValueChange = { textFieldValue = it },
                            placeholder = {
                                Text(stringResource(R.string.enter_registered_email_address),
                                    color = Color(0xFF7F9CA6))
                            },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Person,
                                    contentDescription = "Person Icon",
                                    tint = Color(0xFF7F9CA6))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF7F9CA6), RoundedCornerShape(8.dp)),
                            maxLines = 1,
                            textStyle = TextStyle(
                                color = Color(0xFF35494F),
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.instrument_sans_regular))
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
                        )

                Spacer(modifier = Modifier.height(20.dp))

                // Password
                Text(
                    stringResource(R.string.password),
                    fontSize = 14.sp,
                    fontWeight = FontWeight(400),
                    color = Color(0XFF35494F),
                    fontFamily = FontFamily(Font(R.font.instrument_sans_regular))
                )
                Spacer(Modifier.height(8.dp))

                var passwordTextFieldValue by remember {
                    mutableStateOf(TextFieldValue(text = password, selection = TextRange(password.length)))
                }

                LaunchedEffect(passwordTextFieldValue.text) {
                    if (passwordTextFieldValue.text.startsWith(" ")) {
                        val trimmed = passwordTextFieldValue.text.trimStart()
                        passwordTextFieldValue = passwordTextFieldValue.copy(text = trimmed, selection = TextRange(trimmed.length))
                        password = trimmed
                    } else if (passwordTextFieldValue.text != password) {
                        password = passwordTextFieldValue.text
                    }
                }

                TextField(
                    value = passwordTextFieldValue,
                    onValueChange = { passwordTextFieldValue = it },
                    placeholder = {
                        Text(stringResource(R.string.enter_your_password), color = Color(0xFF7F9CA6))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password Icon",
                            tint = Color(0xFF7F9CA6),
                            modifier = Modifier.size(20.dp))
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Image(
                            painter = painterResource(
                                if (passwordVisible) R.drawable.hide_password else R.drawable.show_password
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { passwordVisible = !passwordVisible }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF7F9CA6), RoundedCornerShape(8.dp)),
                    maxLines = 1,
                    textStyle = TextStyle(
                        color = Color(0xFF35494F),
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.instrument_sans_regular))
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent))

                Spacer(modifier = Modifier.height(30.dp))

                // Login Button
                ProjectButton(buttonName = stringResource(R.string.login)) {
                    if (validation(context, email, password)) {
                        val model = AuthRequest(
                            "password",
                            "099153c2625149bc8ecb3e85e03f00221",
                            password,
                            email)
                        SingletonObject.name = email
                        viewModel.hitLogin(model)
                    }
                }
            }
        }
    }
}

fun observerLogin(
    context: MainActivity,
    viewModel: AuthViewModel,
    navController: NavHostController?,
    lifecycleOwner: LifecycleOwner, ) {
    viewModel.loginLiveData.observe(lifecycleOwner){
        when(it){
            is EmpResource.Failure -> {
                it.throwable?.let { it1 -> ErrorUtil.handlerGeneralError(context, it1) }
                CustomLoader.hideLoader()
            }

            EmpResource.Loading -> {
                CustomLoader.showLoader(context)
            }

            is EmpResource.Success-> {
                CustomLoader.hideLoader()
                Log.d("TAG", "observerLogin: Login success")
                SharedPreference.get(context).accessToken =it.value.access_token.toString()

                //calling merchant details api
                val model =MerchantDetailsRequest(SingletonObject.name.toString())
                viewModel.hitMerchantDetail(/*returnAccessToken(context)*/model)
            }
        }
    }

    viewModel.merchantDetailLiveData.observe(lifecycleOwner){
        when(it){
            is EmpResource.Failure -> {
                it.throwable?.let { it1 -> ErrorUtil.handlerGeneralError(context, it1) }
                CustomLoader.hideLoader()
            }
            EmpResource.Loading -> {
                CustomLoader.showLoader(context)
            }

            is EmpResource.Success-> {
                CustomLoader.hideLoader()
                if(it.value.info.isSuccess==true) {
                    it.value.info.isSuccess=false
                    Log.d("TAG", "observerLogin: Login success")
                    ObjectForTab.tabName=0
                    ObjectForTab.tabRequestName ="Installation"
                    SingletonObject.name=it.value.data?.userName.toString()
                    SharedPreference.get(context).name = it.value.data?.userName.toString()
                    SharedPreference.get(context).email = it.value.data?.email.toString()
                    SharedPreference.get(context).userId = it.value.data?.userId.toString()
                    SharedPreference.get(context).averageRating = it.value.data?.averageRating.toString()

                    //calling fcm token api
                    val fcmModel = FcmRequest(SharedPreference.get(context).fcmToken,SharedPreference.get(context).userId)
                    viewModel.hitFcmToken(returnAccessToken(context),fcmModel)

                    (context as? MainActivity)?.setupSync()
                    navController?.navigate(Screen.DashboardScreen.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }
}

private fun validation(
    context: Context,
   email:String,
    password:String
): Boolean {
    if (email.isNullOrEmpty()) {
        showToastC(context, "Please Enter Username")
        return false
    }

    if (password.isNullOrEmpty()) {
        showToastC(context, "Please Enter Password")
        return false
    }
    return true
}


@Composable
@Preview(showSystemUi = true)
fun prss() {
//    LoginScreen()
}
