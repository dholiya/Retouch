package zest.photoeditorpro.photoeditor.view

import android.graphics.Bitmap
import org.tensorflow.lite.task.vision.detector.Detection

interface canvasInterface {
    public fun returnMask():Bitmap
    public fun clearMask()
    public fun clearOtherMask(layoutPosition: Int, listMask: Detection)
}

