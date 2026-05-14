/*
 * Copyright 2015 - 2021 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.assetinfinity.app.gpsclient

import android.app.Service
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import com.assetinfinity.app.gpsclient.DatabaseHelper.DatabaseHandler
import com.assetinfinity.app.gpsclient.NetworkManager.NetworkHandler
import com.assetinfinity.app.gpsclient.PositionProvider.PositionListener
import com.assetinfinity.app.gpsclient.ProtocolFormatter.formatRequest
import com.assetinfinity.app.gpsclient.RequestManager.RequestHandler
import com.assetinfinity.app.gpsclient.RequestManager.sendRequestAsync

class TrackingController(private val context: Context) :
    PositionListener, NetworkHandler {
    private val sharedPref = context.getSharedPreferences("MyPreferences", Service.MODE_PRIVATE)
    val editor = sharedPref.edit()

    private val handler = Handler(Looper.getMainLooper())
    private var positionProvider = PositionProviderFactory.create(context, this)
    private val databaseHelper = DatabaseHelper(context)
    private val networkManager = NetworkManager(context, this)

    private val url: String = "hgb"
    private val buffer = sharedPref.getBoolean("KEY_BUFFER", true)

    private var isOnline = networkManager.isOnline
    private var isWaiting = false

    fun start() {
        try {
            Log.d(TAG, "start: chhhhh---")
            positionProvider = PositionProviderFactory.create(context, this)

            positionProvider.startUpdates()
        } catch (e: SecurityException) {
            Log.w(TAG, e)
        }
        if (isOnline) {
            read()
        }

        networkManager.start()
    }

    fun stop() {
        networkManager.stop()
        try {
            positionProvider.stopUpdates()
        } catch (e: SecurityException) {
            Log.w(TAG, e)
        }
        handler.removeCallbacksAndMessages(null)
    }

    override fun onPositionUpdate(position: Position) {
        if (buffer) {
            write(position)
        } else {
            send(position)
        }
    }

    override fun onPositionError(error: Throwable) {}
    override fun onNetworkUpdate(isOnline: Boolean) {
//        val message =
//            if (isOnline) R.string.status_network_online else R.string.status_network_offline
        if (!this.isOnline && isOnline) {
            read()
        }
        this.isOnline = isOnline
    }

    //
    // State transition examples:
    //
    // write -> read -> send -> delete -> read
    //
    // read -> send -> retry -> read -> send
    //

    private fun log(action: String, position: Position?) {
        var formattedAction: String = action
        if (position != null) {
            formattedAction +=
                " (id:" + position.id +
                        " time:" + position.time.time / 1000 +
                        " lat:" + position.latitude +
                        " lon:" + position.longitude + ")"
        }
        Log.d("Kavita", formattedAction)
    }

    private fun write(position: Position) {
        log("write", position)
        databaseHelper.insertPositionAsync(position, object : DatabaseHandler<Unit?> {
            override fun onComplete(success: Boolean, result: Unit?) {
                if (success) {
                    if (isOnline && isWaiting) {
                        read()
                        isWaiting = false
                    }
                }
            }
        })
    }

    private fun read() {
        log("read", null)
        databaseHelper.selectPositionAsync(object : DatabaseHandler<Position?> {
            override fun onComplete(success: Boolean, result: Position?) {
                if (success) {
                    if (result != null) {
                        if (result.deviceId == Settings.Secure.getString(
                                context.contentResolver,
                                Settings.Secure.ANDROID_ID
                            )
                        ) {
                            send(result)
                        } else {
                            delete(result)
                        }
                    } else {
                        isWaiting = true
                    }
                } else {
                    retry()
                }
            }
        })
    }

    private fun delete(position: Position) {
        log("delete", position)
        databaseHelper.deletePositionAsync(position.id, object : DatabaseHandler<Unit?> {
            override fun onComplete(success: Boolean, result: Unit?) {
                if (success) {
                    read()
                } else {
                    retry()
                }
            }
        })
    }

    private fun send(position: Position) {
        log("send", position)
        val token: String? = sharedPref.getString("Token", "")
        val request = formatRequest(url, position, token)
        Log.d("sendRequest", "sendRequest:- $request")
        sendRequestAsync(request, object : RequestHandler {
            override fun onComplete(success: Boolean) {
                Log.d("sendResponse", "onComplete:- $success")
                if (success) {
                    if (buffer) {
                        delete(position)
//                        start()
                    }
                } else {
                    if (buffer) {
                        retry()
                    }
                }
            }
        })
    }

    private fun retry() {
        log("retry", null)
        handler.postDelayed({
            if (isOnline) {
                read()
            }
        }, RETRY_DELAY.toLong())
    }

    companion object {
        private val TAG = TrackingController::class.java.simpleName
        private const val RETRY_DELAY = 60 * 5 * 1000
    }

}
