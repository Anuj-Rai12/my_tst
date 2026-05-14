package com.pos10.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.pos10.R
import com.pos10.view.navigation.Screen
import okhttp3.ResponseBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.google.android.gms.location.LocationServices


object CommonUtils {

    fun showToastC(context: Context, message: String) {
        if (message.isNotEmpty())
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

    }

   /* fun parseApiError(errorBody: ResponseBody?): String? {
        return errorBody?.let {
            try {
                val errorBodyString = it.string()
                val jsonObject = JSONObject(errorBodyString)
                val message = jsonObject.optString("error_description") // Use "error_description" for better clarity)
                if (message.isBlank()) null else message // <- fix here
            } catch (e: Exception) {
                null
            }
        }
    }*/

    fun parseApiError(errorBody: ResponseBody?): String? {
        return errorBody?.let {
            try {
                val errorBodyString = it.string()

                // Basic HTML vs JSON check
                if (!errorBodyString.trim().startsWith("{")) {
                    Log.e("API_ERROR", "Non-JSON error: $errorBodyString")
                    return "Something went wrong. Please try again."
                }

                val jsonObject = JSONObject(errorBodyString)
                val message = jsonObject.optString("error_description")
                if (message.isBlank()) null else message
            } catch (e: Exception) {
                Log.e("API_ERROR", "Exception: ${e.localizedMessage}")
                return "Something went wrong. Please try again."
            }
        }
    }


    fun convertLongToDate(timestamp: Long): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC") // Match timezone with the formatter used in reverse
        }
        return formatter.format(Date(timestamp))
    }

    fun convertDateToLong(dateString: String): Long {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")  // Force UTC to avoid shifting
        }
        val date = formatter.parse(dateString)
        return date?.time ?: 0L
    }
    fun convertTimestampToDateNew(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault()) // 12-hour format with AM/PM
        return sdf.format(Date(timestamp))
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun convertTimeToLong(timeString: String): Long {
        val localTime = LocalTime.parse(timeString)
        return Duration.between(LocalTime.MIN, localTime).toMillis()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun checkTime(fromTime: String, toTime: String): Boolean {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        // Define the restricted time range
        if (fromTime != "null" && toTime != "null") {
            val restrictedStartTime = LocalTime.parse(fromTime, formatter)
            val restrictedEndTime = LocalTime.parse(toTime, formatter)

            // Check if the time falls within the restricted range
            if (restrictedStartTime.isAfter(restrictedEndTime) || restrictedEndTime.isBefore(
                    restrictedStartTime
                )
            ) {
                return true
            }

        }
        return false
    }

    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault() // Use device timezone, or set to UTC if needed
        return sdf.format(Date(timestamp))
    }

    fun formatTimestampWithExpirationCheck(timestamp: Long,isBlockd:Boolean?=null): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val currentTime = System.currentTimeMillis() // Get current timestamp
        return if (timestamp < currentTime) {
            "Expired" // Show "Expired" if timestamp is in the past
        } else {
            if(isBlockd == true){
                "InActive"
            }else {
                "Active"
            }
        }
    }

    fun changeDateFormat(inputDate: String): String {
        val originalFormat: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        val targetFormat: SimpleDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        val date: Date
        try {
            date = originalFormat.parse(inputDate)
            return targetFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            return ""
        }
    }

    fun changeDateFormatYYYY(inputDate: String): String {
        val originalFormat: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        val targetFormat: SimpleDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        val date: Date
        try {
            date = originalFormat.parse(inputDate)
            return targetFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            return ""
        }
    }

    fun getCurrentDate(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return currentDate.format(formatter)
    }

    fun formatTime(input: String): String {
        val inputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("h a", Locale.getDefault()) // Format as 10 Am
        val date: Date? = inputFormat.parse(input)
        return date?.let { outputFormat.format(it) } ?: ""
    }

    fun String?.isInvalid(): Boolean {
        return this.isNullOrEmpty() || this.equals("null", ignoreCase = true)
    }

    enum class REQUESTTYPE(val type: Int,val title:String){
        ALLJOBS(0,"all"),
        INSTALLATION(27702,"Installation"),
         COMPLAINT(27703,"Complaint"),
         PAPERROLL(27704,"PaperRoll Delivery"),
        RETURN(27711,"Return"),
        REPLACEMENT(27712,"Replacement"),
    }

    enum class STATUS(val type: Int,val sttausname: String){
        CREATED(type = 2901, sttausname = "Created"),
        ASSIGNED(type = 2912, sttausname = "Assigned"),
        ITEMPACKED(type = 2919, sttausname = "Item Packed"),
        PENDING(type = 2913, sttausname = "Pending"),
        INSTALLED(2908, sttausname = "Installed"),
        INREPAIR(2909, sttausname = "InRepair"),
        INPROGRESS(2910, sttausname = "InProgress"),
        COMPLETED(2911, sttausname = "Completed"),
        FAILED(2914, sttausname = "Failed");

        companion object {
            fun getByType(type: Int): STATUS? {
                return values().firstOrNull { it.type == type }
            }
        }
    }

    enum class Priority(
        val label: String,
        val color: Color,
        @DrawableRes val iconRes: Int) {
        HIGH("High", Color(0xFFEF5350), R.drawable.ic_upward_arrow),
        MEDIUM("Medium", Color(0xFFFFB300), R.drawable.ic_medium_arrow),
        LOW("Low", Color(0xFF43A047), R.drawable.ic_downward_arrow);

        companion object {
            fun fromApiValue(value: Int?): Priority {
                return when (value) {
                    1 -> HIGH
                    2,0 -> MEDIUM
                    3 -> LOW
                    else -> LOW
                }
            }
        }
    }

    enum class SLA(
        val sla: String,
        val color: Color,
       ) {
        ONTIME("On Time", Color(0xFF43A047)),
        ATRISK("At Risk", Color(0xFFFFB300)),
        NEARBREACH("Near Breach", Color(0xFFEF5350)),
        BREACHED("Breached", Color(0xFFEF5350));

        companion object {
            fun fromApiValueSla(value: String?): SLA {
                return when (value) {
                    "On Time" -> ONTIME
                    "At Risk"-> ATRISK
                    "Near Breach" -> NEARBREACH
                    "Breached" -> BREACHED
                    else -> ONTIME
                }
            }
        }
    }

    enum class UPLOADDOCUMENTTYPE(val type: Int,val uploadType: String){
        SERIALID(type = 3901, uploadType = "Serial Number"),
        IDLE(type = 3902, uploadType = "Idle Screen"),
        CASHIER(type = 3903, uploadType = "Cashier Screen"),
        OUTLET(type = 3905, uploadType = "Outlet Image"),
        SIGNATURE(type = 3906, uploadType = "Signature"),
        OTHERS(3904, uploadType = "Others"),
    }

    enum class SHIPPINGTYPE(val type:Int){
        DOMESTIC(1),CONTINENTAL(2),
        WORLDWIDE(3),EXCEPTION_UNTIED_KINGDOM(4)
    }

    @Composable
    fun ShowTextView(
        modifier: Modifier = Modifier,
        text: String,
        fontSize: Int,
        fontWeight: FontWeight,
        color: Color
    ) {
        Text(text = text,
            modifier = modifier,
            color = color,
            fontSize = fontSize.sp,
            fontWeight = fontWeight,)
    }


    fun getLatLongFromAddress(context: Context, address: String): Pair<Double, Double>? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(address, 1)

            if (addresses != null && addresses.isNotEmpty()) {
                val location = addresses[0]
                Pair(location.latitude, location.longitude)
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun returnAccessToken(context: Context): String {
        return "Bearer ${SharedPreference.get(context).accessToken}"
    }

   /* @SuppressLint("MissingPermission")
    fun getSimInfo(context: Context): String {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        return try {
            val simState = telephonyManager.simState
            val operatorName = telephonyManager.networkOperatorName
            val simOperator = telephonyManager.simOperator
            val lineNumber = telephonyManager.line1Number  // May return null

            """
        SIM State: $simState
        Operator Name: $operatorName
        SIM Operator: $simOperator
        Phone Number: ${lineNumber ?: "N/A"}
        """.trimIndent()
        } catch (e: SecurityException) {
            "Permission not granted"
        } catch (e: Exception) {
            "Error getting SIM info: ${e.message}"
        }
    }
*/

    /*val context = LocalContext.current
    val simInfo = remember { mutableStateOf("Fetching SIM Info...") }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        simInfo.value = if (isGranted) {
            getSimInfo(context)
        } else {
            "Permission denied"
        }
    }

    // Check & request permission
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) ==
            PackageManager.PERMISSION_GRANTED) {
            simInfo.value = getSimInfo(context)
        } else {
            permissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
        }
    }*/


    fun convertDateTimeFormat(input: String): String {
        val formats = listOf(
            SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
            )

        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (format in formats) {
            try {
                val date = format.parse(input)
                if (date != null) {
                    return outputFormat.format(date)
                }
            } catch (e: Exception) {
                // ignore and try next format
            }
        }
        return ""
    }

    fun convertDateTimeFormatoffline(input: String): String {

        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
        val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())

        val date = LocalDate.parse(input, inputFormatter)
        return date.format(outputFormatter)
    }

    //18-07-2025 00:00:00
//    fun convertDateTimeToDate(input: String): String {
//        val inputFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
//        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
//
//        val date = inputFormat.parse(input) ?: return ""
//        return outputFormat.format(date)
//    }
    //to maintain offline data format or server
    fun convertDateTimeToDate(input: String): String {
        val formats = listOf(
            SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()))

        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        for (format in formats) {
            try {
                val date = format.parse(input)
                if (date != null) {
                    return outputFormat.format(date)
                }
            } catch (e: Exception) {
                // ignore and try next format
            }
        }
        return ""
    }

    fun convertDateFormat(inputDate: String): String {
        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val date = inputFormat.parse(inputDate)
        return outputFormat.format(date!!)
    }

//    fun convertDateFormatSelected(inputDate: String): String {
//        val formats = listOf(
//            SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()),
//            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
//            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
//            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//        )
//        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
//
//        for (format in formats) {
//            try {
//                val date = format.parse(inputDate)
//                if (date != null) {
//                    return outputFormat.format(date)
//                }
//            } catch (e: Exception) {
//                // ignore and try next
//            }
//        }
//        return ""
//    }
fun convertDateFormatSelected(inputDate: String): String {
    val formats = listOf(
        "dd-MM-yyyy HH:mm:ss",
        "dd/MM/yyyy",
        "dd-MM-yyyy",
        "yyyy-MM-dd"
    )

    val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
        isLenient = false
    }

    for (pattern in formats) {
        try {
            val parser = SimpleDateFormat(pattern, Locale.getDefault()).apply {
                isLenient = false
            }
            val date = parser.parse(inputDate)
            if (date != null) {
                return outputFormat.format(date)
            }
        } catch (_: Exception) {
            // try next format
        }
    }
    return ""
}


    fun formatDate(
        dateStr: String,
        inputPattern: String = "dd-MM-yyyy",
        outputPattern: String = "dd MMM yyyy"
    ): String {
        return try {
            val inputFormat = SimpleDateFormat(inputPattern, Locale.getDefault())
            val outputFormat = SimpleDateFormat(outputPattern, Locale.getDefault())
            val date = inputFormat.parse(dateStr)
            if (date != null) outputFormat.format(date) else ""
        } catch (e: Exception) {
            ""
        }
    }

     fun navigateToLogin(navController: NavHostController) {
        navController.navigate(Screen.LoginScreen.route) {
            popUpTo(Screen.JobsScreen.route) { inclusive = true }
        }
    }

    fun maskEmail(email: String): String {
        val parts = email.split("@")
        if (parts.size != 2) return email

        val name = parts[0]
        val domain = parts[1]

        val maskedName = when {
            name.length <= 2 -> "*".repeat(name.length) // ex: "ab" → "**"
            name.length <= 4 -> name.first() + "*".repeat(name.length - 2) + name.last() // ex: "abcd" → "a**d"
            else -> name.take(2) + "*".repeat(name.length - 4) + name.takeLast(2) // ex: "mnofoqii" → "mn****ii"
        }

        return "$maskedName@$domain"
    }

    fun maskMiddle(input: String): String {
        if (input.length <= 4) return input // too short, skip masking

        val start = input.take(2) // first 2 chars
        val end = input.takeLast(2) // last 2 chars
        val mask = "*".repeat(input.length - 4)

        return start + mask + end
    }

    fun compressImageFile(
        context: Context,
        originalFile: File,
        maxWidth: Int = 1280,
        maxHeight: Int = 1280,
        quality: Int = 70,
        dpi: Int = 72
    ): File {
        // Step 1: Decode bounds only (to get dimensions safely)
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(originalFile.absolutePath, options)

        val originalWidth = options.outWidth
        val originalHeight = options.outHeight
        val originalSizeKB = originalFile.length() / 1024

        Log.d("ImageCompression", "🖼 Original Size: ${originalSizeKB} KB ($originalWidth x $originalHeight)")

        // Step 2: Compute sample size for downsampling large images
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
        options.inJustDecodeBounds = false

        val bitmap = BitmapFactory.decodeFile(originalFile.absolutePath, options)
            ?: throw IllegalArgumentException("Unable to decode file: ${originalFile.path}")

        // Step 3: Resize bitmap to target dimensions
        val ratio = minOf(
            maxWidth.toFloat() / bitmap.width,
            maxHeight.toFloat() / bitmap.height
        )
        val scaledBitmap = Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * ratio).toInt(),
            (bitmap.height * ratio).toInt(),
            true
        )

        // Step 4: Save compressed version
        val compressedFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
        FileOutputStream(compressedFile).use { out ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }

        bitmap.recycle()
        scaledBitmap.recycle()

        // Step 5: Set DPI metadata
        try {
            val exif = androidx.exifinterface.media.ExifInterface(compressedFile)
            exif.setAttribute(ExifInterface.TAG_X_RESOLUTION, dpi.toString())
            exif.setAttribute(ExifInterface.TAG_Y_RESOLUTION, dpi.toString())
            exif.setAttribute(ExifInterface.TAG_RESOLUTION_UNIT, "2") // 2 = inches
            exif.saveAttributes()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Step 6: Log compressed info
        val compressedSizeKB = compressedFile.length() / 1024
        val compOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(compressedFile.absolutePath, compOptions)
        Log.d("ImageCompression", "Compressed Size: ${compressedSizeKB} KB (${compOptions.outWidth} x ${compOptions.outHeight})")

        return compressedFile
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /*fun compressImageFile(context: Context, originalFile: File): File {
        val bitmap = BitmapFactory.decodeFile(originalFile.absolutePath)

        // ✅ Resize if too large
        val maxWidth = 1280
        val maxHeight = 1280
        val ratioBitmap = if (bitmap.width > maxWidth || bitmap.height > maxHeight) {
            val ratio = minOf(
                maxWidth.toFloat() / bitmap.width,
                maxHeight.toFloat() / bitmap.height
            )
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * ratio).toInt(),
                (bitmap.height * ratio).toInt(),
                true
            )
        } else bitmap

        // ✅ Save compressed copy
        val compressedFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
        FileOutputStream(compressedFile).use { out ->
            // Compress to 70 % quality → ideal for upload
            ratioBitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)
        }

        ratioBitmap.recycle()
        return compressedFile
    }*/

    /*@SuppressLint("MissingPermission")
    fun getCurrentAddress(context: Context, onResult: (String?) -> Unit) {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)

        // Fallback for old versions
        fusedClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val address = addresses?.firstOrNull()?.getAddressLine(0)
                    onResult(address)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun getDistanceInKm(context: Context, startAddress: String, endAddress: String, onResult: (Double?) -> Unit) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val startList = geocoder.getFromLocationName(startAddress, 1)
            val endList = geocoder.getFromLocationName(endAddress, 1)

            if (!startList.isNullOrEmpty() && !endList.isNullOrEmpty()) {
                val start = startList[0]
                val end = endList[0]

                val results = FloatArray(1)
                Location.distanceBetween(
                    start.latitude, start.longitude,
                    end.latitude, end.longitude,
                    results
                )

                val distanceKm = results[0] / 1000.0
                onResult(distanceKm)
            } else {
                onResult(null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(null)
        }
    }

*/
    @Composable
    fun DashedDivider(
        color: Color = Color(0xFFF6F6F6),
        thickness: Float = 1f,
        dashLength: Float = 10f,
        gapLength: Float = 5f,
        modifier: Modifier = Modifier
    ) {
        Canvas(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 4.dp)
        ) {
            drawLine(
                color = color,
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                strokeWidth = thickness,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength, gapLength), 0f)
            )
        }
    }

    fun dialPhoneNumber(context: Context, phoneNumber: String?) {
        if (!phoneNumber.isNullOrBlank()) {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "Phone number not available", Toast.LENGTH_SHORT).show()
        }
    }

    /** Reverse geocode current lat/lng into human-readable address */
    fun getAddressFromLatLng(context: Context, lat: Double, lng: Double): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addressList = geocoder.getFromLocation(lat, lng, 1)
            addressList?.firstOrNull()?.getAddressLine(0)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /** Geocode destination address into lat/lng */
    fun getLatLngFromAddress(context: Context, address: String): Pair<Double, Double>? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val locationList = geocoder.getFromLocationName(address, 1)
            val loc = locationList?.firstOrNull()
            if (loc != null) Pair(loc.latitude, loc.longitude) else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /** Compute distance between two coordinate pairs */
    fun getDistanceInKm(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): Double {
        val earthRadius = 6371.0 // km
        val dLat = Math.toRadians(endLat - startLat)
        val dLng = Math.toRadians(endLng - startLng)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(startLat)) * Math.cos(Math.toRadians(endLat)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    fun parseDateToMillis(dateStr: String?): Long? {
        if (dateStr.isNullOrBlank()) return null
        return try {
            val format = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            format.parse(dateStr)?.time
        } catch (e: Exception) {
            null
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(
        context: Context,
        onResult: (Double?, Double?) -> Unit
    ) {
        val client = LocationServices.getFusedLocationProviderClient(context)

        try {
            client.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        onResult(location.latitude, location.longitude)
                    } else {
                        onResult(null, null)
                    }
                }
                .addOnFailureListener {
                    onResult(null, null)
                }
        } catch (e: Exception) {
            onResult(null, null)
        }
    }

    fun getCurrentDateDdMmYyyy(): String {
        return SimpleDateFormat("ddMMyyyy", Locale.getDefault())
            .format(Date())
    }

    enum class SoundDialogStep {
        NONE,
        PLAY_SOUND,
        CONFIRM_PLAY_SOUND,
        CONFIRM_SOUND_PLAYED,
        CONFIRM_UNBIND_DEVICE
    }

    fun bitmapToUri(context: Context, bitmap: Bitmap): Uri {
        val file = File(context.cacheDir, "signature_${System.currentTimeMillis()}.png")

        FileOutputStream(file).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
        }

        return file.toUri()
    }
}