package com.pos10.util

import android.content.Context


class SharedPreferences private constructor(context: Context) {
    private val sharedPreferences =
        context.applicationContext.getSharedPreferences(PRE_NAME, Context.MODE_PRIVATE)


    companion object {
        const val PRE_NAME = "SharePre"

        @Volatile
        private var inference: SharedPreferences? = null
        fun getInstance(context: Context): SharedPreferences {
            return inference ?: synchronized(this) {
                inference ?: SharedPreferences(context).also { inference = it }
            }
        }
    }

    var accessToken: String?
        get() = sharedPreferences.getString("accessToken", null)
        set(value) = sharedPreferences.edit().putString("accessToken", value).apply()

}