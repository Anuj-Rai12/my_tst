package com.pos10.helper

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NetworkService private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: NetworkService? = null

        fun getInstance(context: Context): NetworkService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NetworkService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val _isOnline = MutableLiveData<Boolean>()
    val isOnline: LiveData<Boolean> = _isOnline

    init {
        _isOnline.value = checkInternetConnection()
        registerNetworkCallback()
    }

    private fun registerNetworkCallback() {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(object :
                ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _isOnline.postValue(true)
                }

                override fun onLost(network: Network) {
                    _isOnline.postValue(false)
                }
            })
        } else {
            // For older versions, fallback to checking network status periodically or via broadcast
            // You can implement a BroadcastReceiver if needed
        }
    }

    fun getCurrentStatus(): Boolean {
        return _isOnline.value ?: false
    }

    private fun checkInternetConnection(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
