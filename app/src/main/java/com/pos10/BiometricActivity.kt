package com.pos10

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.pos10.helper.BiometricHelper
import java.util.concurrent.Executor

class BiometricAuthActivity : FragmentActivity() {

    private val TAG = "BiometricAuth"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authenticate()
    }

    private fun authenticate() {
        val biometricManager = BiometricManager.from(this)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ biometric + PIN/pattern fallback
            val canAuth = biometricManager.canAuthenticate(authenticators)
            if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
                finishWithError("No biometric or device credential available")
                return
            }

            val executor: Executor = ContextCompat.getMainExecutor(this)
            val biometricPrompt = BiometricPrompt(
                this,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        finishWithSuccess()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        finishWithError("Authentication Error: $errString")
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Log.w(TAG, "Biometric authentication failed")
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Authentication Required")
                .setSubtitle("Use fingerprint or PIN/pattern to continue")
                .setAllowedAuthenticators(authenticators)
                .build()

            biometricPrompt.authenticate(promptInfo)

        } else {
            // Fallback for Android 10 and below using KeyguardManager
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            if (keyguardManager.isDeviceSecure) {
                val intent = keyguardManager.createConfirmDeviceCredentialIntent(
                    "Authentication Required",
                    "Confirm your screen lock PIN, pattern, or password to continue"
                )
                if (intent != null) {
                    launchCredentialIntent.launch(intent)
                } else {
                    finishWithError("Device credential intent is null")
                }
            } else {
                finishWithError("Device not secured with PIN, pattern, or password")
            }
        }
    }

    private val launchCredentialIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                finishWithSuccess()
            } else {
                finishWithError("Device Credential Authentication Failed or Canceled")
            }
        }

    private fun finishWithSuccess() {
        val data = Intent().apply {
            putExtra(BiometricHelper.EXTRA_RESULT, BiometricHelper.RESULT_SUCCESS)
        }
        setResult(RESULT_OK, data)
        finish()
    }

    private fun finishWithError(error: String) {
        Log.e(TAG, error)
        val data = Intent().apply {
            putExtra(BiometricHelper.EXTRA_RESULT, error)
        }
        setResult(RESULT_CANCELED, data)
        finish()
    }
}

//this code to show
//package com.pay10
//
//import android.content.Intent
//import android.os.Bundle
//import androidx.biometric.BiometricManager
//import androidx.biometric.BiometricPrompt
//import androidx.fragment.app.FragmentActivity
//import androidx.core.content.ContextCompat
//import com.pay10.helper.BiometricHelper
//
//class BiometricAuthActivity : FragmentActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        authenticate()
//    }
//
//    private fun authenticate() {
//        val biometricManager = BiometricManager.from(this)
//        val canAuth = biometricManager.canAuthenticate(
//            BiometricManager.Authenticators.BIOMETRIC_STRONG
//        )
//
//        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
//            finishWithError("Biometric not available or not enrolled")
//            return
//        }
//
//        val executor = ContextCompat.getMainExecutor(this)
//        val biometricPrompt = BiometricPrompt(
//            this,
//            executor,
//            object : BiometricPrompt.AuthenticationCallback() {
//                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
//                    super.onAuthenticationSucceeded(result)
//                    finishWithSuccess()
//                }
//
//                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
//                    super.onAuthenticationError(errorCode, errString)
//                    finishWithError(errString.toString())
//                }
//
//                override fun onAuthenticationFailed() {
//                    super.onAuthenticationFailed()
//                    finishWithError("Authentication failed, please try again")
//                }
//            }
//        )
//
//        val promptInfo = BiometricPrompt.PromptInfo.Builder()
//            .setTitle("Biometric Authentication")
//            .setSubtitle("Verify with your fingerprint")
//            .setNegativeButtonText("Cancel")
//            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
//            .build()
//
//        biometricPrompt.authenticate(promptInfo)
//    }
//
//    private fun finishWithSuccess() {
//        val data = Intent().apply {
//            putExtra(BiometricHelper.EXTRA_RESULT, BiometricHelper.RESULT_SUCCESS)
//        }
//        setResult(RESULT_OK, data)
//        finish()
//    }
//
//    private fun finishWithError(error: String) {
//        val data = Intent().apply {
//            putExtra(BiometricHelper.EXTRA_RESULT, error)
//        }
//        setResult(RESULT_CANCELED, data)
//        finish()
//    }
//}

/*package com.pay10

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.pay10.helper.BiometricHelper
import java.util.concurrent.Executor

class BiometricAuthActivity : FragmentActivity() {

    private val correctPin = "123456" // 6-digit PIN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authenticate()
    }

    private fun authenticate() {
        val biometricManager = BiometricManager.from(this)
        val canAuth = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)

        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            // Biometric not available → show PIN pad
            showPinPadScreen()
            return
        }

        val executor: Executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    finishWithSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.d("BiometricAuth", "Error: $errString")
                    // Show PIN pad as fallback
                    showPinPadScreen()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.d("BiometricAuth", "Authentication failed")
                    showPinPadScreen()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authentication Required")
            .setSubtitle("Use fingerprint to continue")
            .setNegativeButtonText("Use PIN")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun showPinPadScreen() {
        setContent {
            PinPadScreen(
                correctPin = correctPin,
                onPinSuccess = { finishWithSuccess() },
                onPinFailure = { finishWithError("PIN Authentication Failed") }
            )
        }
    }

    private fun finishWithSuccess() {
        val data = Intent().apply {
            putExtra(BiometricHelper.EXTRA_RESULT, BiometricHelper.RESULT_SUCCESS)
        }
        setResult(RESULT_OK, data)
        finish()
    }

    private fun finishWithError(error: String) {
        val data = Intent().apply {
            putExtra(BiometricHelper.EXTRA_RESULT, error)
        }
        setResult(RESULT_CANCELED, data)
        finish()
    }
}

@Composable
fun PinPadScreen(correctPin: String, onPinSuccess: () -> Unit, onPinFailure: () -> Unit) {
    var pin by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101010)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // PIN dots indicators
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(6) { index ->
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                if (pin.length > index) Color.White else Color.Gray,
                                RoundedCornerShape(10.dp)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Keypad layout
            val buttons = listOf(
                listOf("1","2","3"),
                listOf("4","5","6"),
                listOf("7","8","9"),
                listOf("","0","⌫")
            )

            buttons.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    row.forEach { btn ->
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color.DarkGray, RoundedCornerShape(40.dp))
                                .clickable {
                                    when (btn) {
                                        "⌫" -> if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                        "" -> {} // empty button
                                        else -> if (pin.length < 6) pin += btn
                                    }

                                    if (pin.length == 6) {
                                        if (pin == correctPin) onPinSuccess() else onPinFailure()
                                        pin = ""
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(btn, color = Color.White, fontSize = 24.sp)
                        }
                    }
                }
            }
        }
    }
}*/

//custom way
/*
package com.pay10

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.pay10.helper.BiometricHelper
import java.util.concurrent.Executor

class BiometricAuthActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authenticate()
    }

    private fun authenticate() {
        val biometricManager = BiometricManager.from(this)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                // Use BiometricPrompt with device credential fallback on Android 11+
                val canAuth = biometricManager.canAuthenticate(authenticators)
                if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
                    finishWithError("No biometric or device credential available")
                    return
                }

                val executor: Executor = ContextCompat.getMainExecutor(this)
                val biometricPrompt = BiometricPrompt(
                    this,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            finishWithSuccess()
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            finishWithError("Authentication Error: $errString")
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            finishWithError("Authentication Failed")
                        }
                    }
                )

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Authentication Required")
                    .setSubtitle("Use biometric or device PIN/pattern to continue")
                    .setAllowedAuthenticators(authenticators)
                    .build()

                biometricPrompt.authenticate(promptInfo)
            }

            else -> {
                // Fallback for Android 10 and below using KeyguardManager
                val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                if (keyguardManager.isDeviceSecure) {
                    val intent = keyguardManager.createConfirmDeviceCredentialIntent(
                        "Authentication Required",
                        "Confirm your screen lock PIN, pattern, or password to continue"
                    )

                    if (intent != null) {
                        launchCredentialIntent.launch(intent)
                    } else {
                        finishWithError("Device credential intent is null")
                    }
                } else {
                    finishWithError("Device not secured with PIN, pattern, or password")
                }
            }
        }
    }

    private val launchCredentialIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                finishWithSuccess()
            } else {
                finishWithError("Device Credential Authentication Failed or Canceled")
            }
        }

    private fun finishWithSuccess() {
        val data = Intent().apply {
            putExtra(BiometricHelper.EXTRA_RESULT, BiometricHelper.RESULT_SUCCESS)
        }
        setResult(RESULT_OK, data)
        finish()
    }

    private fun finishWithError(error: String) {
        Log.e("BiometricAuth", error)
        val data = Intent().apply {
            putExtra(BiometricHelper.EXTRA_RESULT, error)
        }
        setResult(RESULT_CANCELED, data)
        finish()
    }
}
*/


//device credential way

/*
package com.pay10

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.pay10.helper.BiometricHelper
import java.util.concurrent.Executor

class BiometricAuthActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authenticate()
    }

    private fun authenticate() {
        val biometricManager = BiometricManager.from(this)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

        when {
            // Android 11 (API 30) and above → Biometric + device credential fallback
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                val canAuth = biometricManager.canAuthenticate(authenticators)
                if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
                    finishWithError("No biometric or device credential available")
                    return
                }

                val executor: Executor = ContextCompat.getMainExecutor(this)
                val biometricPrompt = BiometricPrompt(
                    this,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            finishWithSuccess()
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            finishWithError("Authentication Error: $errString")
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            finishWithError("Authentication Failed")
                        }
                    }
                )

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Authentication Required")
                    .setSubtitle("Use fingerprint, face, or device PIN/pattern to continue")
                    .setAllowedAuthenticators(authenticators)
                    .build()

                biometricPrompt.authenticate(promptInfo)
            }

            // Android 10 and below → KeyguardManager fallback
            else -> {
                val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                if (keyguardManager.isDeviceSecure) {
                    val intent = keyguardManager.createConfirmDeviceCredentialIntent(
                        "Authentication Required",
                        "Confirm your screen lock PIN, pattern, or password to continue"
                    )

                    if (intent != null) {
                        launchCredentialIntent.launch(intent)
                    } else {
                        finishWithError("Device credential intent is null")
                    }
                } else {
                    finishWithError("Device not secured with PIN, pattern, or password")
                }
            }
        }
    }

    // For KeyguardManager credential confirmation
    private val launchCredentialIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                finishWithSuccess()
            } else {
                finishWithError("Device Credential Authentication Failed or Canceled")
            }
        }

    private fun finishWithSuccess() {
        val data = Intent().apply {
            putExtra(BiometricHelper.EXTRA_RESULT, BiometricHelper.RESULT_SUCCESS)
        }
        setResult(RESULT_OK, data)
        finish()
    }

    private fun finishWithError(error: String) {
        Log.e("BiometricAuth", error)
        val data = Intent().apply {
            putExtra(BiometricHelper.EXTRA_RESULT, error)
        }
        setResult(RESULT_CANCELED, data)
        finish()
    }
}
*/
