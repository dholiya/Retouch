package zest.photoeditorpro.photoeditor.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import zest.photoeditorpro.photoeditor.ParentActivity.Companion.SCALE
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class Util {


}

fun px2dpi(px: Int): Int {
    return ((px - 0.5f) / SCALE).toInt()
}

fun dpi2px(dpi: Int): Double {
    return (((dpi + 0.5F) * SCALE))
}

fun scaleBitmap(bmp:Bitmap): Bitmap {
    val imageWidth: Int = bmp.getWidth()
    val imageHeight: Int = bmp.getHeight()
    Log.i("saadasd",imageWidth.toString())
    Log.i("saadasd",imageHeight.toString())
    val newHeight = imageHeight * 1080 / imageWidth
    return Bitmap.createScaledBitmap(bmp, 1080, newHeight, false)
}

fun scaleBitmapH(bmp:Bitmap): Bitmap {
    val imageWidth: Int = bmp.getWidth()
    val imageHeight: Int = bmp.getHeight()
    val newwidth = imageWidth * 1768 / imageHeight
    return Bitmap.createScaledBitmap(bmp, newwidth, 1768, false)
}

class TaskRunner {
    private val executor: Executor =
        Executors.newSingleThreadExecutor() // change according to your requirements
    private val handler = Handler(Looper.getMainLooper())

    interface Callback<Any> {
        fun onComplete(result: Any)
    }

    fun <Any> executeAsync(callable: Callable<Any>, callback: Callback<Any>) {
        executor.execute {
            val result = callable.call()

            handler.post {
                callback.onComplete(result)
            }
        }
    }
}

fun saveMediaToStorage(bitmap: Bitmap, context: Context) {
    //Generating a file name
    val filename = "${System.currentTimeMillis()}.jpg"

    //Output stream
    var fos: OutputStream? = null

    //For devices running android >= Q
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        //getting the contentResolver
        context.contentResolver?.also { resolver ->

            //Content resolver will process the contentvalues
            val contentValues = ContentValues().apply {

                //putting file information in content values
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }

            //Inserting the contentValues to contentResolver and getting the Uri
            val imageUri: Uri? =
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            //Opening an outputstream with the Uri that we got
            fos = imageUri?.let { resolver.openOutputStream(it) }
        }
    } else {
        //These for devices running on android < Q
        //So I don't think an explanation is needed here
        val imagesDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val image = File(imagesDir, filename)
        fos = FileOutputStream(image)
    }

    fos?.use {
        //Finally writing the bitmap to the output stream that we opened
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
    }
}





