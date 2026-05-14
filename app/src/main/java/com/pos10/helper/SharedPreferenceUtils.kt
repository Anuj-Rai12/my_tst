package com.pos10.helper

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.pos10.MyApplication
import com.pos10.R


//--------------------------------Define Preference:: DeveloperDaya--------------------------------//

class SharedPreference(context: Context) {

    val preference: SharedPreferences =
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
    val editor: SharedPreferences.Editor = preference.edit()

    companion object {
        const val IS_USER_EXIST = "isUserExist"

        var instance: SharedPreference? = null
        var context: Context? = null
        fun get(ctx: Context): SharedPreference {
            if (instance == null) {
                instance = SharedPreference(ctx)
            }
            context = ctx.applicationContext
            return instance!!
        }
        fun getToken():String{
            return get(context!!).accessToken

        }
    }


    operator fun SharedPreferences.set(key: String, value: Any?) {
        when (value) {
            is Int -> edit { it.putInt(key, value) }
            is String? -> edit { it.putString(key, value) }
            is Boolean -> edit { it.putBoolean(key, value) }
            is Float -> edit { it.putFloat(key, value) }
            is Long -> edit { it.putLong(key, value) }
            else -> Log.e("TAG", "Setting shared pref failed for key: $key and value: $value ")
        }
    }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        operation(editor)
        editor.apply()
    }

    inline operator fun <reified T : Any> SharedPreferences.get(
        key: String, defaultValue: T? = null
    ): T? {
        return when (T::class) {
            String::class -> getString(key, defaultValue as? String) as T?
            Int::class -> getInt(key, defaultValue as? Int ?: -1) as T?
            Boolean::class -> getBoolean(key, defaultValue as? Boolean ?: false) as T?
            Float::class -> getFloat(key, defaultValue as? Float ?: -1f) as T?
            Long::class -> getLong(key, defaultValue as? Long ?: -1) as T?
            else -> throw UnsupportedOperationException("Not yet implemented")
        }
    }

    //--------------------------------Define Your Global Variable Here:: DeveloperDaya--------------------------------//
    var mobileNumber: String
        get() = preference["mobileNumber", ""] ?: ""
        set(value) = preference.set("mobileNumber", value)

    var countryCode:String
        get() = preference["countryCode", ""] ?: ""
        set(value) = preference.set("countryCode", value)

    var deviceToken: String
        get() = preference["device_token", ""] ?: ""
        set(value) = preference.set("device_token", value)


    var name: String
        get() = preference["name", ""] ?: ""
        set(value) = preference.set("name", value)

    var roleId: String
        get() = preference["roleId", ""] ?: ""
        set(value) = preference.set("roleId", value)

    var userId: String
        get() = preference["userId", ""] ?: ""
        set(value) = preference.set("userId", value)

    var averageRating: String
        get() = preference["averageRating", ""] ?: ""
        set(value) = preference.set("averageRating", value)

    var contact_number: String
        get() = preference["contact_number", ""] ?: ""
        set(value) = preference.set("contact_number", value)

    var deviceType: String
        get() = preference["device_type", ""] ?: ""
        set(value) = preference.set("device_type", value)

    var language: String
        get() = preference["language", ""] ?: ""
        set(value) = preference.set("language", value)

    var email: String
        get() = preference["email", ""] ?: ""
        set(value) = preference.set("email", value)

    var loginFrom: String
        get() = preference["loginFrom", ""] ?: ""
        set(value) = preference.set("loginFrom", value)

    var accessToken: String
        get() = preference["access_token", ""] ?: ""
        set(value) = preference.set("access_token", value)

    var access_token_type: String
        get() = preference["token_type", ""] ?: ""
        set(value) = preference.set("token_type", value)

    var currencySymbol: String
        get() = preference["currency_symbol", ""] ?: ""
        set(value) = preference.set("currency_symbol", value)

    var skip: Boolean
        get() = preference["skip", false] ?: false
        set(value) = preference.set("skip", value)

    var isLogin: Boolean
        get() = preference["isLogin", false] ?: false
        set(value) = preference.set("isLogin", value)

    var latitude: String
        get() = preference["latitude", ""]?:""
        set(value) = preference.set("latitude", value)

    var longitude: String
        get() = preference["longitude", ""]?:""
        set(value) = preference.set("longitude", value)

    var destinationAddress: String
        get() = preference["destinationAddress", ""]?:""
        set(value) = preference.set("destinationAddress", value)

    var fcmToken: String
        get() = preference["fcmToken", ""]?:""
        set(value) = preference.set("fcmToken", value)

    var isNotificationStatus: Boolean
        get() = preference["isNotificationStatus", false] ?: false
        set(value) = preference.set("isNotificationStatus", value)


}