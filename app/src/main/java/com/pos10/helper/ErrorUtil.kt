package com.pos10.helper

import android.content.Context
import com.pos10.helper.CommonUtils.parseApiError
import com.pos10.helper.CommonUtils.showToastC
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


object ErrorUtil {
    fun handlerGeneralError(context: Context, throwable: Throwable) {
        throwable.printStackTrace()
        when (throwable) {
            is ConnectException -> showToastC(context, "Please turn on Internet")
            is SocketTimeoutException -> showToastC(context, "Socket Time Out Exception")
            is UnknownHostException -> showToastC(context, "No Internet Connection")
            is InternalError -> showToastC(context, "Internal Server Error")
            is HttpException -> {
               /* val errorMessage = parseApiError(throwable.response()?.errorBody())
                showToastC(context, errorMessage ?: "")*/

                val code = throwable.code()
                val errorMessage = parseApiError(throwable.response()?.errorBody())

                when (code) {
                    422 -> {
                        showToastC(context, errorMessage ?: "")
                    }
                    400 -> {
                        showToastC(context, errorMessage ?: "")
                    }
                    401 -> {
                        showToastC(context, "Unauthorized access")
                    }
                    500 -> {
                        showToastC(context, "Server error, please try again later")
                    }
                    else -> {
                        showToastC(context, errorMessage ?: "")
                    }
                }
            }

            else -> {
                showToastC(context, "Something went wrong")
            }
        }
    }
}


