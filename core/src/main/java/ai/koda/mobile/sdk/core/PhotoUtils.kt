package ai.koda.mobile.sdk.core

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream

class PhotoUtils(
    private val context: Context
) {
    fun handleBitmap(
        bitmap: Bitmap
    ): Uri? {
        var fos: FileOutputStream? = null
        try {
            val dir = context.cacheDir
            val cacheFile = File(dir, "${System.currentTimeMillis()}_image.jpeg")
            fos = FileOutputStream(cacheFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            return cacheFile.toUri()
        } catch (e: Exception) {
            Log.e("KodaBotsSDK", e.message ?: "Cannot save temporary image")
            return null
        } finally {
            try {
                fos?.close()
            } catch (e: Exception) {
                Log.e("KodaBotsSDK", e.message ?: "Cannot close FileOutputStream")
            }
        }
    }
}