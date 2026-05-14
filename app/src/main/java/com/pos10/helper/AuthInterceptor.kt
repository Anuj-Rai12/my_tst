package com.pos10.helper

import android.util.Log
import com.pos10.MyApplication
import okhttp3.Interceptor
import okhttp3.Response


class HeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val userId = SharedPreference.get(MyApplication.appContext).userId
        Log.d("HeaderInterceptor", "UserId: $userId") // Debug log

        val requestBuilder = chain.request().newBuilder()
            .addHeader("Accept", "application/json")

        if (!userId.isNullOrEmpty()) {
            requestBuilder.addHeader("UserName", userId)
        }

        return chain.proceed(requestBuilder.build())
    }
}