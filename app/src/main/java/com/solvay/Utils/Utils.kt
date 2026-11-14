package com.solvay.Utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.MediaStore
import android.telephony.TelephonyManager
import android.text.InputFilter
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.auth0.android.jwt.JWT

import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.solvay.BuildConfig
import com.solvay.MainApplication
import com.solvay.R
import com.solvay.authentication.models.Location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.coroutines.resume
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


object Utils {
    val TAG = Utils::class.java.name
    var dialog: Dialog? = null
    var daysDiff: Long = 0L
    var miliSecond: Long = 0L
    var monthsDiff: Long = 0L
    var yearsDiff: Long = 0L
    var seconds: Long = 0L
    var minutes: Long = 0L
    var hours: Long = 0L
    var amPm: String = ""
    var finalDateFormats: String = ""
    var time: String = ""
    var timeProperFormat: String = ""
    var date: String = ""
    var yearMonthDate: String = ""
    var finalDateFormat: String = ""
    var messageDateMonth: String = ""


    fun isTokenExpired(): Boolean {
        val token: String = Prefs.init().accessToken
        return run {
            val jwt = JWT(token)
            Log.e(
                TAG,
                "isTokenExpired: " + System.currentTimeMillis() + "-" + jwt.expiresAt!!
                    .time + "=" + (System.currentTimeMillis() - jwt.expiresAt!!.time)
            )
            if (System.currentTimeMillis() > jwt.expiresAt!!.time && System.currentTimeMillis() - jwt.expiresAt!!
                    .time > 0
            ) {
                Log.e(TAG, "isTokenExpired: true")
                true
            } else {
                Log.e(TAG, "isTokenExpired: false")
                false
            }
        }
    }

    inline fun runWithDelay(delay: Long, crossinline run: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed({
            run()
        }, delay)
    }

    fun View.showView() {
        visibility = View.VISIBLE
        animate()
            .alpha(1.0f)
            .setDuration(500)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)

                }
            })
    }
    /**To load image on background before setting to image view*/
    @SuppressLint("UseCompatLoadingForDrawables")
    suspend fun ImageView.loadImageAsync(url: String, drawable: Int = R .drawable.ic_greyuser) {
        val bitmap: Bitmap? = withContext(Dispatchers.IO) {
            try {
                // Perform the image loading operation here (e.g., using Glide, Picasso, or any other image loading library)
                // For example, using Glide:
                Glide.with(this@loadImageAsync)
                    .asBitmap()
                    //      .apply(RequestOptions().override(10, 10))
                    .load(url)
                    .error(drawable)
                    .submit()
                    .get()
            } catch (e: Exception) {
                // Handle exceptions if any
                drawableToBitmap(MainApplication.get().applicationContext.getDrawable(drawable)!!)
            }
        }

        // Set the loaded image to ImageView on the main thread
        bitmap?.let {
            withContext(Dispatchers.Main) {
                setImageBitmap(it)
            }
        }
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    suspend fun ImageView.loadImageMessage(url: String) {
        val bitmap: Bitmap? = withContext(Dispatchers.IO) {
            try {
                // Perform the image loading operation here (e.g., using Glide, Picasso, or any other image loading library)
                // For example, using Glide:
                Glide.with(this@loadImageMessage)
                    .asBitmap()
                    //      .apply(RequestOptions().override(10, 10))
                    .encodeQuality(10)
                    .load(url)
                    .error(R.drawable.ic_greyuser)
                    .submit()
                    .get()
            } catch (e: Exception) {
                // Handle exceptions if any
                Log.e("failedToLoadImage", "Failed Load Image: $url")
                drawableToBitmap(MainApplication.get().applicationContext.getDrawable(R.drawable.ic_greyuser)!!)

            }
        }

        // Set the loaded image to ImageView on the main thread
        bitmap?.let {
            withContext(Dispatchers.Main) {
                setImageBitmap(it)
            }
        }
    }
    fun convertDateAndTime(dateTimeString: String) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)

            /** Parse the input timestamp to Date object*/
            val data = inputFormat.parse(dateTimeString)

            // Format the date to the desired output format
            val formattedDate = data?.let { outputFormat.format(it) }

            /** Convert UTC formatted date to local time*/
            val df = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)
            df.timeZone = TimeZone.getTimeZone("UTC")
            val utcDate = formattedDate?.let { df.parse(it) }
            df.timeZone = TimeZone.getDefault()
            val localFormattedDate = utcDate?.let { df.format(it) }

            /** Get hours/time information here*/
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
            time = localFormattedDate?.let { df.parse(it)?.let { it1 -> timeFormat.format(it1) } }!!

            val timeFormatOne = SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH)
            timeProperFormat = localFormattedDate.let {
                df.parse(it)
                    ?.let { it1 -> timeFormatOne.format(it1) }
            }!!

            /** Get year-month-days(2001-01-01) information here*/
            val yearMonth = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            yearMonthDate = localFormattedDate.let {
                df.parse(it)
                    ?.let { it1 -> yearMonth.format(it1) }
            }!!

            /** Get year-month-days (01Jan2001) information here*/
            val yearMonths = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            finalDateFormat = localFormattedDate.let {
                df.parse(it)
                    ?.let { it1 -> yearMonths.format(it1) }
            }!!

            /** Get year-month-days (01Jan2001) information here*/
            val yearsMonths = SimpleDateFormat("MMM dd , yyyy", Locale.ENGLISH)
            messageDateMonth = localFormattedDate.let {
                df.parse(it)
                    ?.let { it1 -> yearsMonths.format(it1) }
            }!!
            /** Get AM/PM information here*/
            val amPmFormat = SimpleDateFormat("a", Locale.ENGLISH)
            amPm = localFormattedDate.let { df.parse(it)?.let { it1 -> amPmFormat.format(it1) } }!!


            /** Get year-month-days (01Jan2001) information here*/
            val yearsMonth = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.ENGLISH)
            finalDateFormats = localFormattedDate.let {
                df.parse(it)
                    ?.let { it1 -> yearsMonth.format(it1) }
            }!!


            /** Calculate time differences*/
            val cal1: Calendar = Calendar.getInstance()
            val cal2: Calendar = Calendar.getInstance()
            cal2.time = df.parse(localFormattedDate)!!

            /** Now, you have daysDiff, monthsDiff, seconds, minutes, hours, and amPm available*/
            val msDiff: Long = cal1.timeInMillis - cal2.timeInMillis
            daysDiff = TimeUnit.MILLISECONDS.toDays(msDiff)
            monthsDiff = (daysDiff / 30.44).toLong()
            yearsDiff = (daysDiff / 365.25).toLong()
            seconds = TimeUnit.MILLISECONDS.toSeconds(msDiff)
            minutes = TimeUnit.MILLISECONDS.toMinutes(msDiff)
            hours = TimeUnit.MILLISECONDS.toHours(msDiff)
            miliSecond = TimeUnit.MILLISECONDS.toMillis(msDiff)
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle the parsing exception
        }
    }
    //    fun convertDateAndTime(dateTimeString: String) {
//        try {
//            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
//            val outputFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)
//
//            // Parse the input timestamp to Date object
//            val data = inputFormat.parse(dateTimeString)
//
//            // Format the date to the desired output format
//            val formattedDate = data?.let { outputFormat.format(it) }
//
//            // Convert UTC formatted date to local time
//            val df = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)
//            df.timeZone = TimeZone.getTimeZone("UTC")
//            val utcDate = df.parse(formattedDate)
//            df.timeZone = TimeZone.getDefault()
//            val localFormattedDate = utcDate?.let { df.format(it) }
//
//            // Get hours/minutes information
//            val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
//            time = localFormattedDate?.let { timeFormat.format(df.parse(it)) }!!
//            // Get year information
//            val yearMonth = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
//            yearMonthDate = localFormattedDate?.let { yearMonth.format(df.parse(it)) }!!
//
//            // Get AM/PM information
//            val amPmFormat = SimpleDateFormat("a", Locale.ENGLISH)
//            amPm = localFormattedDate?.let { amPmFormat.format(df.parse(it)) }!!
//
//            // Calculate time differences
//            val cal1: Calendar = Calendar.getInstance()
//            val cal2: Calendar = Calendar.getInstance()
//            cal2.time = df.parse(localFormattedDate)
//
//            val  msDiff: Long = cal1.timeInMillis - cal2.timeInMillis
//            daysDiff = TimeUnit.MILLISECONDS.toDays(msDiff)
//            monthsDiff = (daysDiff / 30.44).toLong()
//            seconds = TimeUnit.MILLISECONDS.toSeconds(msDiff)
//            minutes = TimeUnit.MILLISECONDS.toMinutes(msDiff)
//            hours = TimeUnit.MILLISECONDS.toHours(msDiff)
//
//            // Now, you have daysDiff, monthsDiff, seconds, minutes, hours, and amPm available
//            println("AM/PM: $amPm")
//        } catch (e: Exception) {
//            e.printStackTrace()
//            // Handle the parsing exception
//        }
//    }
    fun ignoreFirstWhiteSpace(et: EditText, limit: Int): Array<InputFilter> {
        val filter: InputFilter = object : InputFilter {
            var canEnterSpace = false
            override fun filter(
                source: CharSequence, start: Int, end: Int,
                dest: Spanned, dstart: Int, dend: Int,
            ): CharSequence {
                if (et.text.toString() == "") {
                    canEnterSpace = false
                }
                val builder = StringBuilder()
                for (i in start until end) {
                    val currentChar = source[i]
                    if (Character.isLetterOrDigit(currentChar) || currentChar == '_') {
                        builder.append(currentChar)
                        canEnterSpace = true
                    }
                    if (Character.isWhitespace(currentChar) && canEnterSpace) {
                        builder.append(currentChar)
                    }
                }
                return builder.toString()
            }
        }
        return arrayOf(filter, InputFilter.LengthFilter(limit), ignoreSpecialCharacters())
    }

    fun ignoreSpecialCharacters(): InputFilter {
        val filter =
            InputFilter { source, start, end, dest, dstart, dend ->
                var filtered = ""
                for (i in start until end) {
                    val character = source[i]
                    if (Character.isWhitespace(character) || Character.isLetterOrDigit(character)) {
                        filtered += character
                    }
                }
                filtered
            }
        return filter
    }
    fun View.disableMultiTap5(duration: Long = 200) {
        try {
            isEnabled = false
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                isEnabled = true
            }, duration)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun View.disableMultiTap(duration: Long = 1000) {
        try {
            isEnabled = false
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                isEnabled = true
            }, duration)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun View.disableMultiTapForThreeSeconds(duration: Long = 3000) {
        try {
            isEnabled = false
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                isEnabled = true
            }, duration)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun View.messgaegDisableMultiTap() {
        try {
            isEnabled = false
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                isEnabled = true
            }, 2000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun View.fileDownLoadDisableMultiTap() {
        try {
            isEnabled = false
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                isEnabled = true
            }, 3000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun Any.toJsonString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }

    inline fun <reified T> String.fromJsonToGson(): T {
        return Gson().fromJson(this, T::class.java)
    }

    inline fun <reified T> String?.fromJsonToList(): List<T>? {
        if (this.isNullOrEmpty()) return null

        val type = object : TypeToken<List<T>>() {}.type
        return Gson().fromJson<List<T>>(this, type)
    }



    /** Save Image In Device after crop the image */
    fun Context.saveImage(bitmap: Bitmap): Uri? {
        var uri: Uri? = null
        try {
            val fileName = System.nanoTime().toString() + ".png"
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                } else {
                    val directory =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    val file = File(directory, fileName)
                    put(MediaStore.MediaColumns.DATA, file.absolutePath)
                }
            }

            uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                contentResolver.openOutputStream(it).use { output ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, output!!)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.apply {
                        clear()
                        put(MediaStore.Audio.Media.IS_PENDING, 0)
                    }
                    contentResolver.update(uri, values, null, null)
                }
            }
            return uri
        } catch (e: java.lang.Exception) {
            if (uri != null) {
                // Don't leave an orphan entry in the MediaStore
                contentResolver.delete(uri, null, null)
            }
            throw e
        }
    }


    fun Context.deleteImages(uris: List<Uri>) {
        for (uri in uris) {
            try {
                val deletedRows = contentResolver.delete(uri, null, null)
                if (deletedRows > 0) {
                    Log.d("DeleteImages", "Image deleted successfully: $uri")
                } else {
                    Log.e("DeleteImages", "Failed to delete image: $uri")
                }
            } catch (e: Exception) {
                Log.e("DeleteImages", "Error deleting image: $uri", e)
            }
        }
    }


    /**  Load BitMap From View */
    fun loadBitmapFromView(v: View): Bitmap {
        val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        v.draw(c)
        return b
    }

    fun isInternetAvailables(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    fun <T> toJsonObject(vararg pairs: Pair<String, T>): JSONObject {
        val parentObject = JSONObject()
        pairs.forEach { pair ->
            parentObject.put(pair.first, pair.second)
        }
        return parentObject
    }



    /**To load image on background before setting to image view*/

    /**convert drawable resource to bitmap*/
    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }


    /** For description conversion*/
    fun convertDesc(desc: String): String {
        if (desc.isNullOrEmpty()) {
            return ""
        }

        val regex = Regex("@\\[([^\\]]+)]\\(([^)]+)\\)")
//        val regex = Regex("@\\w+")
        val outputText = regex.replace(desc) { result ->
            "@${result.groupValues[1]}"
        }

        return outputText
    }


    fun getUsernames(description: String): ArrayList<String> {
        val mentions = ArrayList<String>()
        val regex = Regex("@\\[([^]]+)]\\(([^)]+)\\)")

        val matches = regex.findAll(description)
        matches.forEach { mention ->
            val mentionText = mention.groupValues[0]
            val mentionName = mentionText.substringAfter("[").substringBefore("]")
                .substringAfter("@")
            mentions.add(mentionName)
        }
        return mentions
    }

    fun getSpans(body: String?, prefix: Char): ArrayList<IntArray> {
        val spans = ArrayList<IntArray>()
        val pattern: Pattern = Pattern.compile("$prefix\\w+")
        val matcher: Matcher = pattern.matcher(body.toString())

        // Check all occurrences
        while (matcher.find()) {
            val currentSpan = IntArray(2)
            currentSpan[0] = matcher.start()
            currentSpan[1] = matcher.end()
            spans.add(currentSpan)
        }
        return spans


    }

    fun highLightInBlue(mentionUserWithText: String): SpannableStringBuilder {
        var spannableString = convertDesc(mentionUserWithText)
        val spannableStringBuilder = SpannableStringBuilder(spannableString)

// Find and highlight mentions
        val pattern = Regex("@[a-zA-Z0-9_]+") // Regular expression pattern to find mentions
        val matches = pattern.findAll(spannableString)

        for (match in matches) {
            val start = match.range.first
            val end = match.range.last

            // Apply color to mentions
            spannableStringBuilder.setSpan(
                ForegroundColorSpan(Color.BLUE),
                start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return spannableStringBuilder
    }

    suspend fun downloadFileFromUrl(
        context: Context,
        fileUrl: String,
        fileName: String,
        type: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(fileUrl)
                val connection: URLConnection = url.openConnection()
                connection.connect()

                if (connection.getInputStream() != null) {
                    val contentResolver = context.contentResolver

                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        if (type == "video")
                            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                        else
                            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    }

                    val collection =
                        if (type == "video") MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        else MediaStore.Images.Media.EXTERNAL_CONTENT_URI

                    val itemUri = contentResolver.insert(collection, contentValues)

                    itemUri?.let { newUri ->
                        contentResolver.openOutputStream(newUri)?.use { outputStream ->
                            connection.getInputStream().use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }

                        // Notify media scanner for gallery visibility using file path
                        MediaScannerConnection.scanFile(
                            context,
                            arrayOf(File(newUri.path).absolutePath),
                            null,
                            null
                        )

                        // Alternatively, trigger a media scan broadcast
                        context.sendBroadcast(
                            Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, newUri)
                        )

                        return@withContext true
                    } ?: run {
                        return@withContext false
                    }
                } else {
                    return@withContext false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext false
            }
        }
    }

    fun showError(context: Context,message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    fun getTimeFromDateTime(dateTimeString: String): String {
        // Define input format (ISO 8601)
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")

        // Parse the date
        val date: Date? = inputFormat.parse(dateTimeString)

        // Define output format (only time, like 10:58 AM)
        val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        outputFormat.timeZone = TimeZone.getDefault()

        return if (date != null) {
            outputFormat.format(date).uppercase()
        } else {
            ""
        }
    }

    fun getDateTime(dateTimeString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")

        val date: Date? = try {
            inputFormat.parse(dateTimeString)
        } catch (e: Exception) {
            null
        }

        if (date != null) {
            val calendar = Calendar.getInstance()
            val today = Calendar.getInstance()
            val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            calendar.time = date

            return when {
                isSameDay(calendar, today) -> {
                    val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    outputFormat.timeZone = TimeZone.getDefault()
                    outputFormat.format(date).uppercase()
                }
                isSameDay(calendar, yesterday) -> {
                    "Yesterday"
                }
                else -> {
                    val outputFormat = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
                    outputFormat.format(date)
                }
            }
        } else {
            return ""
        }
    }

    fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }


    val languageMap = mapOf(
        "german" to "de",
        "english" to "en",
        "polish" to "pl",
        "russian" to "ru",
        "ukrainian" to "uk",
        "czech" to "cs",
        "hungarian" to "hu",
        "slovak" to "sk",
        "slovenian" to "sl",
        "croatian" to "hr",
        "bulgarian" to "bg",
        "romanian" to "ro",
        "lithuanian" to "lt",
        "french" to "fr",
        "spanish" to "es",
        "italian" to "it"
    )
    suspend fun translateText(message: String, targetLanguage: String): String = suspendCancellableCoroutine { continuation ->
        if (message.isEmpty()) {
            continuation.resume("")
            return@suspendCancellableCoroutine
        }

        val langCode = languageMap[targetLanguage.lowercase()] ?: targetLanguage
        val apiKey = BuildConfig.TRANSLATION_KEY
        val url = "https://translation.googleapis.com/language/translate/v2?key=$apiKey"

        val json = JSONObject()
        json.put("q", message)
        json.put("target", langCode)

        val body = RequestBody.create("application/json; charset=utf-8".toMediaType(), json.toString())
        val request = Request.Builder().url(url).post(body).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Translation request failed: ${e.message}")
                continuation.resume(message) // Fallback to original message
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.e(TAG, "Translation request failed with code: ${response.code}")
                        continuation.resume(message)
                        return
                    }

                    val responseBody = response.body?.string()
                    responseBody?.let {
                        try {
                            val translatedText = JSONObject(it)
                                .getJSONObject("data")
                                .getJSONArray("translations")
                                .getJSONObject(0)
                                .getString("translatedText")
                            continuation.resume(translatedText)
                        } catch (e: JSONException) {
                            Log.e(TAG, "JSON parsing error: ${e.message}")
                            continuation.resume(message)
                        }
                    } ?: run {
                        Log.e(TAG, "Response body is null")
                        continuation.resume(message)
                    }
                }
            }
        })
    }
}
fun preloadImage(context: Context, data: Location, onComplete: () -> Unit) {
    val fullImageUrl = BuildConfig.IMAGE_URL + (data.image ?: "")
    if (data.image.isNullOrEmpty()) {
        Log.e("ImagePreload", "Image URL is null or empty")
        return
    }

    val isGif = data.image?.endsWith(".gif", ignoreCase = true) == true

    if (isGif) {
        Glide.with(context)
            .asDrawable()
            .load(fullImageUrl)
            .into(object : CustomTarget<Drawable>() {


                override fun onLoadCleared(placeholder: Drawable?) {}

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    Log.e("ImagePreload", "Failed to load GIF: $fullImageUrl")
                    onComplete()
                }

                override fun onResourceReady(
                    resource: Drawable,
                    transition: com.bumptech.glide.request.transition.Transition<in Drawable>?
                ) {
                    data.imageDrawable = resource
                    onComplete()
                }
            })
    } else {
        Glide.with(context)
            .asBitmap()
            .load(fullImageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {}

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    Log.e("ImagePreload", "Failed to load image: $fullImageUrl")
                    onComplete()
                }

                override fun onResourceReady(
                    resource: Bitmap,
                    transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                ) {
                    data.imageBitmap = resource
                    onComplete()
                }
            })
    }
}