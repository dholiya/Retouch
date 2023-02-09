package zest.photoeditorpro.photoeditor

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface

import org.opencv.android.OpenCVLoader
import org.opencv.core.Core

const val PERMISSION_REQUEST_STORAGE = 1


class ParentActivity : AppCompatActivity() {
    private val TAG = "ParentActivity"
    var time = System.currentTimeMillis()

    //    var THRESHOULD = 3000L
    var THRESHOULD = 500L

    companion object{
        var SCALE = 0.0;
    }


    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> {
                    time = System.currentTimeMillis() - time
                    THRESHOULD = THRESHOULD - time
                    Log.d(TAG, "ocv loaded successfully $THRESHOULD")
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parent)
        SCALE  = resources.displayMetrics.density.toDouble()

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }, THRESHOULD)
    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using ocv Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback)
        } else {
            Log.d(TAG, "ocv library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }


    fun checkPermissionAndOpenGallery(activity: Activity): Boolean {
        if (checkSelfPermissionCompat(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        ) {
            return true;
        } else {
            Log.i("permission", "missing");
            // Permission is missing and must be requested.
            requestStoragePermission(activity)
        }
        return false;
    }

    fun requestStoragePermission(activity: Activity) {
        // Permission has not been granted and must be requested.
        if (shouldShowRequestPermissionRationaleCompat(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            Log.i("permission", "request");
            requestPermissionsCompat(
                activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_STORAGE
            )
        } else {
            Toast.makeText(
                activity,
                "Please allow storage permission from settings ",
                Toast.LENGTH_SHORT
            ).show()
            requestPermissionsCompat(
                activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_STORAGE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                openGallery()

            } else {
//                 Permission request was denied.
            }
        }
    }


    fun checkSelfPermissionCompat(activity: Activity, permission: String) =
        ActivityCompat.checkSelfPermission(activity, permission)

    fun shouldShowRequestPermissionRationaleCompat(
        activity: Activity,
        permission: String
    ) =
        ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)

    fun requestPermissionsCompat(
        activity: Activity,
        permissionsArray: Array<String>,
        requestCode: Int
    ) {
        ActivityCompat.requestPermissions(activity, permissionsArray, requestCode)
    }
}