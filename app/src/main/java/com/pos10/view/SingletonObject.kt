package com.pos10.view

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.mutableStateOf

object SingletonObject {
    var fromWhere:String =""
    var name: String=""
    val savedBitmap = mutableStateOf<Bitmap?>(null)
    val savedBitmapQR = mutableStateOf<Bitmap?>(null)
    var uploadImageUriBusiness1= mutableStateOf<Uri?>(null)
    var uploadSerialId= mutableStateOf<Uri?>(null)
    var uploadIdleId= mutableStateOf<Uri?>(null)
    var uploadCashierId= mutableStateOf<Uri?>(null)
    val savedSignOffline = mutableStateOf<String>("")

    fun clear() {
        fromWhere=""
        name = ""
    }
}