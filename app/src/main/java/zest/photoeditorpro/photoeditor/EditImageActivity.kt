package zest.photoeditorpro.photoeditor

import android.graphics.Bitmap
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.doOnLayout
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.segmenter.Segmentation
import zest.photoeditorpro.photoeditor.adapter.DetectedAdapter
import zest.photoeditorpro.photoeditor.adapter.ToolsAdapter
import zest.photoeditorpro.photoeditor.data.Tools
import zest.photoeditorpro.photoeditor.data.getToolList
import zest.photoeditorpro.photoeditor.databinding.ActivityEditImageBinding
import zest.photoeditorpro.photoeditor.helper.ImageSegmentationHelper
import zest.photoeditorpro.photoeditor.helper.ObjectDetectorHelper
import zest.photoeditorpro.photoeditor.util.Str
import zest.photoeditorpro.photoeditor.util.scaleBitmap
import zest.photoeditorpro.photoeditor.util.scaleBitmapH
import zest.photoeditorpro.photoeditor.view.DetectedInterface
import zest.photoeditorpro.photoeditor.view.RDImageView
import java.util.*
import kotlin.math.roundToInt


//https://thenounproject.com/icon/flip-2825113/

class EditImageActivity : AppCompatActivity(), ToolsAdapter.OnToolSelected,
    Slider.OnChangeListener, ObjectDetectorHelper.DetectorListener,
    ImageSegmentationHelper.SegmentationListener, DetectedInterface,
    DetectedAdapter.OnMaskSelected {


    private val TAG: String = "EditImageActivity"
    private lateinit var objectDetectorHelper: ObjectDetectorHelper
    private lateinit var imageSegmentationHelper: ImageSegmentationHelper

    var isToolSelected = false;
    var layoutPosition: Int? = null;
    private var toolsAdapter: ToolsAdapter? = null
    private lateinit var binding: ActivityEditImageBinding
    private lateinit var imgViewCustom: RDImageView;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toolsAdapter = ToolsAdapter(this, getToolList(resources), this, isToolSelected)
        binding.rvTools.adapter = toolsAdapter
        binding.sliderBrush.addOnChangeListener(this);
        binding.sliderErase.addOnChangeListener(this);
        imgViewCustom = RDImageView(this)


        imgViewCustom = binding.imgViewCustom
        imgViewCustom.setImageURI(Uri.parse(intent.extras?.getString("data")))

        val orignal = scaleBitmapH((imgViewCustom.drawable as BitmapDrawable?)!!.bitmap as Bitmap)

        var bmp = orignal;
        if (orignal.width < orignal.height) {
            bmp = scaleBitmapH((imgViewCustom.drawable as BitmapDrawable?)!!.bitmap as Bitmap)
        } else {
            bmp = scaleBitmap((imgViewCustom.drawable as BitmapDrawable?)!!.bitmap as Bitmap)
        }

        imgViewCustom.setImageBitmap(bmp);
        if (bmp.width < bmp.height) {
            binding.outerLy.doOnLayout {
                Log.i("itheight", it.height.toString())
                Log.i("itheight", bmp.height.toString())


                Log.i("itheight w", it.width.toString())
                Log.i("itheight w", bmp.width.toString())

                if (it.height < bmp.height) {
                    imgViewCustom.layoutParams.height = it.height
                } else {
                    imgViewCustom.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                }
            }
            imgViewCustom.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        } else {

            binding.outerLy.doOnLayout {
                Log.i("itheight 0", it.width.toString())
                Log.i("itheight 0", bmp.width.toString())
                if (it.width < bmp.width) {
                    imgViewCustom.layoutParams.width = it.width
                } else {
                    imgViewCustom.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                }
            }
            imgViewCustom.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

        }


//        Handler(Looper.getMainLooper()).postDelayed({
//            binding.rvTools.findViewHolderForAdapterPosition(1)!!.itemView.performClick()
//        }, 3)
        objectDetectorHelper = ObjectDetectorHelper(
            context = this,
            objectDetectorListener = this)
        imageSegmentationHelper = ImageSegmentationHelper(
            context = this,
            imageSegmentationListener = this
        )

        imgViewCustom.setEventListener(this);

//        imgViewCustom.setEventListener(object : DetectedInterface {
//            override fun detecteToMask(leftC: Int, topC: Int, rightC: Int, bottomC: Int) {
//                Log.i("topfds", topC.toString())
//            }
//        });

    }


    override fun onToolSelected(
        tools: Tools,
        tool: LinearLayoutCompat,
        layoutPosition: Int,
    ) {

        // dont call notify if you are slecting tool first time
        if (this.layoutPosition != null) {
            binding.rvTools.itemAnimator!!.changeDuration = 0
            binding.rvTools.itemAnimator!!.addDuration = 0
            binding.rvTools.itemAnimator!!.removeDuration = 0
            binding.rvTools.adapter!!.notifyItemChanged(this.layoutPosition!!)
        }

        tool.setBackgroundResource(R.drawable.selected_tool)
        closeAll()

        when (tools.id) {
            Str.CROP -> {
                isToolSelected = true
                binding.subCrop.visibility = View.VISIBLE
            }
            Str.AUTO -> {
                imgViewCustom.editMode = Str.AUTO
                binding.progress.visibility = View.VISIBLE
                val originalBmp =
                    (binding.imgViewCustom.drawable as BitmapDrawable?)!!.bitmap as Bitmap
                objectDetectorHelper.detect(originalBmp)
//                imageSegmentationHelper.segment(originalBmp)

                isToolSelected = true
                binding.subAuto.visibility = View.VISIBLE
            }
            Str.BRUSH -> {
                isToolSelected = true
                imgViewCustom.editMode = Str.BRUSH
                binding.subBrush.visibility = View.VISIBLE
            }
            Str.ERASE -> {
                isToolSelected = true
                imgViewCustom.editMode = Str.ERASE
                binding.subErase.visibility = View.VISIBLE
            }
        }

        //this.layoutPosition == layoutPosition means you are clicking on save tool which is already selected
        if (this.layoutPosition == layoutPosition) {
            closeAll()
            this.layoutPosition = null
        } else {
            this.layoutPosition = layoutPosition
        }

    }

    fun closeAll() {
        isToolSelected = false
        imgViewCustom.editMode = Str.MODE_NONE
        binding.subCrop.visibility = View.GONE
        binding.subAuto.visibility = View.GONE
        binding.subBrush.visibility = View.GONE
        binding.subErase.visibility = View.GONE
    }

    private fun editMode() {
        binding.rvTools.visibility = View.GONE
        binding.include.appBarSave.visibility = View.GONE

        binding.includesave.appBar.visibility = View.VISIBLE
    }

    private fun editModeBack() {
        binding.rvTools.visibility = View.VISIBLE
        binding.include.appBarSave.visibility = View.VISIBLE

        binding.includesave.appBar.visibility = View.GONE
    }

    override fun onBackPressed() {
        when (1 == 1) {
            true -> {
                super.onBackPressed()
            }
            else -> {
                val alertDialogBuilder = AlertDialog.Builder(this)
                alertDialogBuilder.setMessage("Are you sure!!")
                    .setCancelable(false)
                    .setPositiveButton("YES") { dialog, id -> finish() }
                    .setNegativeButton("NO") { dialog, id -> dialog.cancel() }

                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            }
        }
    }

    override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
        if (imgViewCustom.getEditMode() == Str.BRUSH) {
            binding.sliderBrushSize.requestLayout()
            binding.sliderBrushSize.layoutParams.height = value.toInt()
            binding.sliderBrushText.text = value.toInt().toString()
            imgViewCustom.mPaint.strokeWidth = value;
        } else {
            binding.sliderEraseSize.requestLayout()
            binding.sliderEraseSize.layoutParams.height = value.toInt()
            binding.sliderEraseText.text = value.toInt().toString()
            imgViewCustom.mErasePaint.strokeWidth = value;
        }

    }


    override fun onError(error: String) {
        Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
    }

    override fun onResults(
        results: MutableList<Detection>?,
        inferenceTime: Long,
        imageHeight: Int,
        imageWidth: Int,
    ) {
        binding.progress.visibility = View.GONE;
        imgViewCustom.setDetetionResults(results ?: LinkedList<Detection>(),
            imageHeight,
            imageWidth)
    }

    override fun onSegmentError(error: String) {
        Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
    }

    override fun onSegmentResults(
        results: List<Segmentation>?,
        inferenceTime: Long,
        imageHeight: Int,
        imageWidth: Int,
    ) {
        binding.progress.visibility = View.GONE;
        val originalBmp = (imgViewCustom.drawable as BitmapDrawable?)!!.bitmap as Bitmap

        imgViewCustom.setSegmentResults(results ?: LinkedList<Segmentation>(),
            imageHeight,
            imageWidth,
            originalBmp
        )
    }


    override fun detecteToMask(boundingBox: RectF) {

        var originalBmp = (imgViewCustom.drawable as BitmapDrawable?)!!.bitmap as Bitmap
        var l = 0;
        var t = 0;
        var w = 0;
        var h = 0;
        if (boundingBox.left < 0) l = 0 else l = boundingBox.left.roundToInt()
        if (boundingBox.top < 0) t = 0 else t = boundingBox.top.roundToInt()

        if (boundingBox.height() > originalBmp.height) h = originalBmp.height else h =
            boundingBox.height().roundToInt()
        if (boundingBox.width() > originalBmp.width) w = originalBmp.width else w =
            boundingBox.width().roundToInt()

        val cop = Bitmap.createBitmap(originalBmp, l, t, w, h)

//        imageSegmentationHelper.segment(cop)

    }

    override fun detecteCrop(detectionResults: MutableList<Detection>, scaleFactor: Float) {
        val list = ArrayList<Bitmap>()
        val listMask = ArrayList<Detection>()

        for (result in detectionResults) {

            val boundingBox = result.boundingBox

            val top = boundingBox.top * scaleFactor
            val bottom = boundingBox.bottom * scaleFactor
            val left = boundingBox.left * scaleFactor
            val right = boundingBox.right * scaleFactor

            val originalBmp = (imgViewCustom.drawable as BitmapDrawable?)!!.bitmap as Bitmap

            var l = 0;
            var t = 0;
            var w = 0;
            var h = 0;
            if (boundingBox.left < 0) l = 0 else l = boundingBox.left.roundToInt()
            if (boundingBox.top < 0) t = 0 else t = boundingBox.top.roundToInt()
            Log.i("sadas boundingBox.height()", (boundingBox.height() + t).toString())
            Log.i("sadas originalBmp.height", originalBmp.height.toString())


            if ((boundingBox.height() + t) >= originalBmp.height) h = originalBmp.height else h =
                boundingBox.height().roundToInt()
            if ((boundingBox.width() + l) > originalBmp.width) w = originalBmp.width else w =
                boundingBox.width().roundToInt()

            Log.i("sadas originalBmp.height dd", h.toString())
            Log.i("sadas originalBmp.height dd O",
                originalBmp.getScaledHeight(originalBmp.height).toString())

            val cop = Bitmap.createBitmap(originalBmp, l, t, w, h)
            list.add(cop)
            listMask.add(result)
        }
        val temp = DetectedAdapter(this, list, listMask, this)
        binding.detectedMask.adapter = temp
        binding.subAuto.visibility = View.VISIBLE

    }

    override fun onMaskSelected(bitmap: Bitmap, listMask: Detection, layoutPosition: Int) {
        imageSegmentationHelper.segment(bitmap)
//        imgViewCustom.clearOtherMask(layoutPosition,listMask)
    }

}