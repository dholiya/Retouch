/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zest.photoeditorpro.photoeditor.helper

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.segmenter.ImageSegmenter
import org.tensorflow.lite.task.vision.segmenter.OutputType
import org.tensorflow.lite.task.vision.segmenter.Segmentation
import kotlin.reflect.typeOf

/**
 * Class responsible to run the Image Segmentation model. more information about the DeepLab model
 * being used can be found here:
 * https://ai.googleblog.com/2018/03/semantic-image-segmentation-with.html
 * https://github.com/tensorflow/models/tree/master/research/deeplab
 *
 * Label names: 'background', 'aeroplane', 'bicycle', 'bird', 'boat', 'bottle', 'bus', 'car', 'cat',
 * 'chair', 'cow', 'diningtable', 'dog', 'horse', 'motorbike', 'person', 'pottedplant', 'sheep',
 * 'sofa', 'train', 'tv'
 */
class ImageSegmentationHelper2(
    var numThreads: Int = 3,
    var currentDelegate: Int = 0,
    var currentModel: Int = 1,
    val context: Context,
    val imageSegmentationListener: SegmentationListener2?,
) {
    private var imageSegmenter: ImageSegmenter? = null

    init {
        setupImageSegmenter2()
    }

    fun clearImageSegmenter2() {
        imageSegmenter = null
    }

    private fun setupImageSegmenter2() {
        // Create the base options for the segment
        val optionsBuilder =
            ImageSegmenter.ImageSegmenterOptions.builder()

        // Set general segmentation options, including number of used threads
        val baseOptionsBuilder = BaseOptions.builder().setNumThreads(numThreads)

        // Use the specified hardware for running the model. Default to CPU
        when (currentDelegate) {
            DELEGATE_CPU -> {
                // Default
            }
            DELEGATE_GPU -> {
                if (CompatibilityList().isDelegateSupportedOnThisDevice) {
                    baseOptionsBuilder.useGpu()
                } else {
                    imageSegmentationListener?.onSegmentError2("GPU is not supported on this device")
                }
            }
            DELEGATE_NNAPI -> {
                baseOptionsBuilder.useNnapi()
            }
        }

        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())

        /*
        CATEGORY_MASK is being specifically used to predict the available objects
        based on individual pixels in this sample. The other option available for
        OutputType, CONFIDENCE_MAP, provides a gray scale mapping of the image
        where each pixel has a confidence score applied to it from 0.0f to 1.0f
         */
        val modelName =
            when (currentModel) {
                MODEL_DEEPLABV3 -> "deeplabv3.tflite"
                MODEL_DEEPcoco -> "deepcocodr.tflite"
                MODEL_DEEfm -> "deepdm.tflite"
                else -> "deeplabv3.tflite"
            }

        optionsBuilder.setOutputType(OutputType.CATEGORY_MASK)
        try {
            imageSegmenter =
                  ImageSegmenter.createFromFileAndOptions(
                    context,
                    modelName,
                    optionsBuilder.build()
                )

        } catch (e: IllegalStateException) {
            imageSegmentationListener?.onSegmentError2(
                "Image segmentation failed to initialize. See error logs for details"
            )
            Log.e(TAG, "TFLite failed to load model with error: " + e.message)
        }
    }

    fun segment2(image: Bitmap) {

        if (imageSegmenter == null) {
            setupImageSegmenter2()
        }

        // Inference time is the difference between the system time at the start and finish of the
        // process
        var inferenceTime = SystemClock.uptimeMillis()

        // Create preprocessor for the image.
        // See https://www.tensorflow.org/lite/inference_with_metadata/
        //            lite_support#imageprocessor_architecture
//        val imageProcessor =
//            ImageProcessor.Builder()
//                .add(Rot90Op(-imageRotation / 90))
//                .build()

        // Preprocess the image and convert it into a TensorImage for segmentation.
//        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(image))

        val tensorImage = TensorImage.fromBitmap(image)

        val segmentResult = imageSegmenter?.segment(tensorImage)
        inferenceTime = SystemClock.uptimeMillis() - inferenceTime

        imageSegmentationListener?.onSegmentResults2(
            segmentResult,
            inferenceTime,
            tensorImage.height,
            tensorImage.width
        )
    }

    interface SegmentationListener2 {
        fun onSegmentError2(error: String)
        fun onSegmentResults2(
            results: List<Segmentation>?,
            inferenceTime: Long,
            imageHeight: Int,
            imageWidth: Int,
        )
    }

    companion object {
        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1
        const val DELEGATE_NNAPI = 2
        const val MODEL_DEEPLABV3 = 0
        const val MODEL_DEEPcoco = 1
        const val MODEL_DEEfm = 2
        private const val TAG = "Image Segmentation Helper"
    }
}
