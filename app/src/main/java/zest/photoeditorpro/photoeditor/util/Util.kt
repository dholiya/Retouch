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
import androidx.core.net.toUri
import zest.photoeditorpro.photoeditor.ParentActivity.Companion.SCALE
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class Util {
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


    var width = 1080
    var height = 1768

    fun scaleBitmap(bmp:Bitmap): Bitmap {
        val imageWidth: Int = bmp.getWidth()
        val imageHeight: Int = bmp.getHeight()
        Log.i("saadasd",imageWidth.toString())
        Log.i("saadasd",imageHeight.toString())
        val newHeight = imageHeight * width / imageWidth
        return Bitmap.createScaledBitmap(bmp, width, newHeight, false)
    }

    fun scaleBitmapH(bmp:Bitmap): Bitmap {
        val imageWidth: Int = bmp.getWidth()
        val imageHeight: Int = bmp.getHeight()
        val newwidth = imageWidth * height / imageHeight
        return Bitmap.createScaledBitmap(bmp, newwidth, height, false)
    }

}

fun px2dpi(px: Int): Int {
    return ((px - 0.5f) / SCALE).toInt()
}

fun dpi2px(dpi: Int): Double {
    return (((dpi + 0.5F) * SCALE))
}
//



fun saveMediaToStorage(bitmap: Bitmap, context: Context): Uri? {
    //Generating a file name
    val filename = "${System.currentTimeMillis()}.jpg"

    var file:Uri? =null
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
            file = imageUri;
            //Opening an outputstream with the Uri that we got
            fos = imageUri?.let { resolver.openOutputStream(it) }
        }
    } else {
        //These for devices running on android < Q
        //So I don't think an explanation is needed here
        val imagesDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val image = File(imagesDir, filename)
        file= image.toUri()
        fos = FileOutputStream(image)
    }

    fos?.use {
        //Finally writing the bitmap to the output stream that we opened
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
    }


    return file


}







