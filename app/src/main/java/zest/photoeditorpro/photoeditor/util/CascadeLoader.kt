package zest.photoeditorpro.photoeditor.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import org.opencv.objdetect.CascadeClassifier
import zest.photoeditorpro.photoeditor.R
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class CascadeLoader {
    fun loadEye(resources: Resources, context: Context): CascadeClassifier? {
        val mCascadeFile: File;


        val `is`: InputStream = resources.openRawResource(R.raw.cascade)
        val cascadeDir: File = ContextWrapper(context).getDir("cascade", Context.MODE_PRIVATE)
        mCascadeFile = File(cascadeDir, "cascade.xml")
        val os = FileOutputStream(mCascadeFile)


        val buffer = ByteArray(4096)
        var bytesRead: Int
        while (`is`.read(buffer).also { bytesRead = it } != -1) {
            os.write(buffer, 0, bytesRead)
        }
        `is`.close()
        os.close()

        val detector = CascadeClassifier(mCascadeFile.getAbsolutePath());
        if (detector.empty()) {
            return null;
        } else {
            return detector;
        }
    }
}