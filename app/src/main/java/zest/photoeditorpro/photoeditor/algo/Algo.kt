package zest.photoeditorpro.photoeditor.algo


import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.COLOR_RGBA2RGB
import org.opencv.imgproc.Imgproc.INTER_CUBIC
import org.opencv.objdetect.CascadeClassifier
import zest.photoeditorpro.photoeditor.ParentActivity.Companion.SCALE
import zest.photoeditorpro.photoeditor.util.dpi2px

//https://github.com/mesutpiskin/opencv-object-detection/blob/master/src/FaceAndEyeDetection/DetectFace.java
//https://towardsdatascience.com/how-to-detect-objects-in-real-time-using-opencv-and-python-c1ba0c2c69c0#:~:text=OpenCV%20has%20a%20bunch%20of,object%20as%20per%20our%20need.
class Algo {
    val input = Mat();




    fun detectorArray(local: Bitmap?, detector: CascadeClassifier): Array<Rect>? {
        val result: Bitmap = local!!.copy(local.config, true)
        Utils.bitmapToMat(result, input)
        Imgproc.cvtColor(input, input, COLOR_RGBA2RGB)

        val objects = MatOfRect();
        try {
            val newC = Mat()
            Imgproc.resize(input, newC, Size(dpi2px(result.width), dpi2px(result.height))); // upscale 2x
            detector.detectMultiScale(newC, objects, SCALE);
            Log.i("sadassdas",objects.toArray().size.toString())

            return objects.toArray()
        }catch (e: Exception){
            Log.i("sadassdas",e.toString())
        }
        return  null;
    }


    fun detectorBitmap(local: Bitmap?, detector: CascadeClassifier, mask: Bitmap): Array<Rect> {

        val result: Bitmap = local!!.copy(mask.config, true)
        Utils.bitmapToMat(result, input)

        val maskinput = Mat(input.rows(), input.cols(), input.type())

        Imgproc.cvtColor(input, input, COLOR_RGBA2RGB)
        val objects = MatOfRect();
        detector.detectMultiScale(input, objects)

        val array: Array<Rect> = objects.toArray()

        Log.i("sizea",array.size.toString())
        for (obje in array) {
            Imgproc.rectangle(maskinput, obje, Scalar(200.0, 155.0, 0.0,150.0), 3);
        }
        Imgproc.resize(maskinput, maskinput, Size(input.width().toDouble(), input.height().toDouble()))
        Utils.matToBitmap(maskinput, result)

        return array;
    }





}


