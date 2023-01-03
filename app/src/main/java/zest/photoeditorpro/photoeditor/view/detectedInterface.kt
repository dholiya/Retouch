package zest.photoeditorpro.photoeditor.view

import android.graphics.Bitmap
import android.graphics.RectF
import android.util.AttributeSet
import org.tensorflow.lite.task.vision.detector.Detection

interface DetectedInterface  {
    public fun detecteToMask(boundingBox: RectF, i: Int)
    fun detecteCrop(detectionResults: MutableList<Detection>)
}