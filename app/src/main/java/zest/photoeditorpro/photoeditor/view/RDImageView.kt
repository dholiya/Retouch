package zest.photoeditorpro.photoeditor.view


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.Paint.FILTER_BITMAP_FLAG
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.segmenter.Segmentation
import zest.photoeditorpro.photoeditor.EditImageActivity.Companion.objectDetected
import zest.photoeditorpro.photoeditor.EditImageActivity.Companion.objectSelected
import zest.photoeditorpro.photoeditor.util.Str
import kotlin.math.max
import kotlin.math.roundToInt


class RDImageView : AppCompatImageView, canvasInterface {
    private val initialTapeline: Tapeline = Tapeline()
    private val transformTapeline: Tapeline = Tapeline()
    private val initialPoint = SparseArray<PointF>()

    var lastPointer = 0
    var lastAction = 0

    var filterDetected: MutableList<Detection> = mutableListOf<Detection>();


    private var scaleFactor: Float = 1f

    private var time = System.currentTimeMillis()
    private val ZOOM_TIME_DIFF_THRESHOLD = 400

    private var startX = 0f
    private var startY = 0f

    var editMode = Str.MODE_NONE;


    var scaleF = 0f
    var px = 0f
    var py = 0f
    var dx = 0f
    var dy = 0f

    var mPaint = Paint()
    var mErasePaint = Paint()
    var clearCanvas = false


    companion object {
        lateinit var mbitmap: Bitmap
        lateinit var detectedBoxBitmap: Bitmap
        lateinit var detectedBoxCanvas: Canvas
        lateinit var mcanvas: Canvas

        var fit_height = 0
        var fit_width = 0

    }


    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initCon()
    }

    constructor(context: Context) : super(context) {
        initCon()
    }


    fun initCon() {
        //opencv
//        mPaint.setARGB(150, 255, 99, 99)
        //clipdrop
        mPaint.setARGB(255, 255, 255, 255)
        mPaint.strokeWidth = 25F
        mPaint.isAntiAlias = true
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.style = Paint.Style.STROKE
        mPaint.flags = FILTER_BITMAP_FLAG
        mPaint.isDither = true
        setFocusable(true);


        mErasePaint.alpha = 0
        mErasePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        mErasePaint.isAntiAlias = true
        mErasePaint.isDither = true
        mErasePaint.style = Paint.Style.STROKE
        mErasePaint.strokeJoin = Paint.Join.ROUND
        mErasePaint.strokeCap = Paint.Cap.ROUND
        mErasePaint.strokeWidth = 25F

        this.setLayerType(LAYER_TYPE_SOFTWARE, null)


    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        fit_height = h
        fit_width = w

        mbitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        detectedBoxBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        mcanvas = Canvas()
        detectedBoxCanvas = Canvas()

        mcanvas.setBitmap(mbitmap)
        detectedBoxCanvas.setBitmap(detectedBoxBitmap)
        mcanvas.drawColor(Color.TRANSPARENT)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save();
        if (clearCanvas) {
            mcanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            detectedBoxCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            clearCanvas = false;
        }
        canvas.drawBitmap(mbitmap, 0f, 0f, null)
        canvas.drawBitmap(detectedBoxBitmap, 0f, 0f, null)
        canvas.restore();

    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (event!!.pointerCount == 1) {
            if (editMode.equals(Str.BRUSH) || editMode.equals(Str.ERASE)) {

                val i = event.actionIndex
                val point = initialPoint[event.getPointerId(i)]

                if (point != null) {
                    dx = event.getX(i) - point.x;
                    dy = event.getY(i) - point.y
                }

                if (lastAction != MotionEvent.ACTION_POINTER_UP) {
                    drawLine(event)
                } else if (lastAction == MotionEvent.ACTION_POINTER_UP && System.currentTimeMillis() - time > ZOOM_TIME_DIFF_THRESHOLD) {
                    drawLine(event)
                    lastAction = event.action
                }
            } else if (editMode.equals(Str.AUTO)) {

                var i = 0;
                for (result in objectDetected) {

                    val boundingBox = result.boundingBox

                    val top = boundingBox.top * scaleFactor
                    val bottom = boundingBox.bottom * scaleFactor
                    val left = boundingBox.left * scaleFactor
                    val right = boundingBox.right * scaleFactor


                    dx = event.getX()
                    dy = event.getY()

                    if ((left < dx && dx < right) && (top < dy && dy < bottom)) {
                        Log.i("tblr", "yes")

                        if (System.currentTimeMillis() - time > ZOOM_TIME_DIFF_THRESHOLD) {
                            objectSelected = boundingBox
                            detectedInterface!!.detecteToMask(boundingBox, i)
                            time = System.currentTimeMillis()
//                            editMode = Str.MODE_NONE
                        }
                        break
                    } else {
                        Log.i("tblr", "no")
                    }
                    i++
                }
            }

        } else {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    initTransform(event, -1)
                    Log.i("data", "M ACTION_DOWN")
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    transform(event)
                    Log.i("data", "M ACTION_Move")
                    return true
                }
                MotionEvent.ACTION_POINTER_UP -> {
                    initTransform(event, event.actionIndex)
                    Log.i("data", "M ACTION_POINTER_UP")
                    lastAction = MotionEvent.ACTION_POINTER_UP
                    time = System.currentTimeMillis()
                    return true
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> return true
            }

        }

        return true
    }

    private fun drawLine(event: MotionEvent) {
        val endX = event.x
        val endY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = endX
                startY = endY
            }
            MotionEvent.ACTION_MOVE -> {
                if (Math.abs(startX - endX) > 2 || Math.abs(startX - y) > 2)
                    mcanvas.drawLine(startX,
                        startY,
                        endX,
                        endY,
                        if (editMode.equals(Str.ERASE)) mErasePaint else mPaint)
                startX = endX
                startY = endY
                this.postInvalidate()
            }
            MotionEvent.ACTION_CANCEL -> {}
            MotionEvent.ACTION_UP -> {
            }

        }
    }

    class Tapeline() {
        public var length = 0f
        public var pivotX = 0f
        public var pivotY = 0f
        public operator fun set(event: MotionEvent, p1: Int, p2: Int) {
            val x1 = event.getX(p1)
            val y1 = event.getY(p1)
            val x2 = event.getX(p2)
            val y2 = event.getY(p2)
            val dx = x2 - x1
            val dy = y2 - y1
            length = Math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
            pivotX = (x1 + x2) * .5f
            pivotY = (y1 + y2) * .5f

        }
    }

    private fun initTransform(event: MotionEvent, ignoreIndex: Int) {
        var p1 = 0xffff
        var p2 = 0xffff
        var i = 0
        val l = event.pointerCount
        lastPointer = l;
        while (i < l) {
            initialPoint.put(
                event.getPointerId(i), PointF(
                    event.getX(i),
                    event.getY(i)
                )
            )
            if (i == ignoreIndex) {
                ++i
                continue
            } else if (p1 == 0xffff) {
                p1 = i
            } else {
                p2 = i
                break
            }
            ++i
        }
        if (p2 != 0xffff) {
            initialTapeline.set(event, p1, p2)
        }
    }

    private fun transform(event: MotionEvent) {

        val pointerCount = event.pointerCount
        lastPointer = pointerCount;

        if (pointerCount == 1) {
            val i = event.actionIndex
            val point = initialPoint[event.getPointerId(i)]
            if (point != null) {
                px = event.getX(i) - point.x;
                py = event.getY(i) - point.y
            }
        } else if (pointerCount > 1) {
            transformTapeline.set(event, 0, 1)
            val scale = transformTapeline.length / initialTapeline.length;

            scaleF *= scale;
            scaleF = Math.max(1.0F, Math.min(scaleF, 4.0F))

            px = initialTapeline.pivotX
            py = initialTapeline.pivotY

            dx = transformTapeline.pivotX - initialTapeline.pivotX
            dy = transformTapeline.pivotY - initialTapeline.pivotY

            val maxDx: Float = (getWidth() - getWidth() / scaleF) / 2 * scaleF
            val maxDy: Float = (getHeight() - getHeight() / scaleF) / 2 * scaleF
            dx = Math.min(Math.max(dx, -maxDx), maxDx)
            dy = Math.min(Math.max(dy, -maxDy), maxDy)
        }

        setScaleX(scaleF)
        setScaleY(scaleF)

        setTranslationX(dx)
        setTranslationY(dy)

        setPivotX(px)
        setPivotY(py)
    }


    override fun returnMask(): Bitmap {
        return mbitmap;
    }

    override fun clearMask() {
        invalidate()
        clearCanvas = true
    }


    @JvmName("getEditMode1")
    fun getEditMode(): Int {
        return editMode;
    }

    //object detect
    fun setDetetionResults(
        detectionResults: MutableList<Detection>,
        imageHeight: Int,
        imageWidth: Int,
    ) {
        invalidate()
        filterDetected.clear()
        objectDetected.clear()
        mPaint.setARGB(255, 25, 114, 120)
        mPaint.strokeWidth = 4F

        // PreviewView is in FILL_START mode. So we need to scale up the bounding box to match with
        // the size that the captured images will be displayed.
        scaleFactor = max(width * 1f / imageWidth, height * 1f / imageHeight)


        for (result in detectionResults) {

            result.boundingBox

            val top = result.boundingBox.top * scaleFactor
            val bottom = result.boundingBox.bottom * scaleFactor
            val left = result.boundingBox.left * scaleFactor
            val right = result.boundingBox.right * scaleFactor


            if (result.boundingBox.left + result.boundingBox.width() > imageWidth) {
                result.boundingBox.right = imageWidth.toFloat();
            }
            if (result.boundingBox.top + result.boundingBox.height() > imageHeight) {
                result.boundingBox.bottom = imageHeight.toFloat();
            }
            if (result.boundingBox.left < 0) {
                result.boundingBox.left = 0F
            }
            if (result.boundingBox.top < 0) {
                result.boundingBox.top = 0F
            }

            filterDetected.add(result)
            val rect = RectF(left, top, right, bottom)
            detectedBoxCanvas.drawRect(rect, mPaint)
        }

        objectDetected = filterDetected;

        detectedInterface!!.detectedObjsShowBottom(filterDetected)

        //opencv
        mPaint.setARGB(150, 255, 99, 99)
        //clipdrop
        mPaint.strokeWidth = 25F
    }


    //mask
    fun setSegmentResults(
        segmentResult: List<Segmentation>,
        imageHeight: Int,
        imageWidth: Int,
        originalBmp: Bitmap,
    ) {
        invalidate()
        Log.i("ssisiis", segmentResult.size.toString())
        if (segmentResult.isNotEmpty()) {
            val colorLabels = segmentResult[0].coloredLabels.mapIndexed { index, coloredLabel ->
                ColorLabel(
                    index,
                    coloredLabel.getlabel(),
                    coloredLabel.argb
                )
            }

            // Create the mask bitmap with colors and the set of detected labels.
            // We only need the first mask for this sample because we are using
            // the OutputType CATEGORY_MASK, which only provides a single mask.
            val maskTensor = segmentResult[0].masks[0]
            val maskArray = maskTensor.buffer.array()
            val pixels = IntArray(maskArray.size)


            maskArray.forEachIndexed { i, mask ->
                // Set isExist flag to true if any pixel contains this color.
                val colorLabel = colorLabels[mask.toInt()].apply {
                    isExist = true
                }
                val color = colorLabel.getColor()
                pixels[i] = color
            }

            val image = Bitmap.createBitmap(
                pixels,
                maskTensor.width,
                maskTensor.height,
                Bitmap.Config.ARGB_8888
            )

            val maskScalUp = 0.025

            val scaleBitmap = Bitmap.createScaledBitmap(image,
                (objectSelected.width() + objectSelected.width()*maskScalUp).toInt(),
                (objectSelected.height() +objectSelected.height()*maskScalUp).toInt(),
                false)
            val l = (objectSelected.left) -objectSelected.width()*maskScalUp/2
            val r = (objectSelected.top) -objectSelected.height()*maskScalUp/2

            mcanvas.drawBitmap(scaleBitmap,
                (l.roundToInt()).toFloat(),
                (r.roundToInt()).toFloat(),
                null)
        }

    }


    data class ColorLabel(
        val id: Int,
        val label: String,
        val rgbColor: Int,
        var isExist: Boolean = false,
    ) {

        fun getColor(): Int {
            // Use completely transparent for the background color.
            return if (id == 0) Color.TRANSPARENT else
//                clipdrop
//                Color.WHITE
//                opencv
                Color.argb(
                150,
                Color.red(rgbColor),
                Color.green(rgbColor),
                Color.blue(rgbColor)
            )
        }
    }


    private var detectedInterface: DetectedInterface? = null

    fun setEventListener(detectedInterface: DetectedInterface) {
        this.detectedInterface = detectedInterface
    }


}

