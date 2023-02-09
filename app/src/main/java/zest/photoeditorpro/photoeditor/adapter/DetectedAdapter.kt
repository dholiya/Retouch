package zest.photoeditorpro.photoeditor.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import org.tensorflow.lite.task.vision.detector.Detection
import zest.photoeditorpro.photoeditor.R
import java.util.ArrayList

class DetectedAdapter(
    private val onMaskSelected: OnMaskSelected,
    val imglist: List<Bitmap>,
    val listMask: ArrayList<Detection>,
    val context: Context,
) :
    RecyclerView.Adapter<DetectedAdapter.ViewHolder>() {


    interface OnMaskSelected {
        fun onMaskSelected(bitmap: Bitmap, listMask: Detection, layoutPosition: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.image)
        val name: TextView = itemView.findViewById(R.id.name)

        init {
            itemView.setOnClickListener {
                onMaskSelected.onMaskSelected(
                    imglist[layoutPosition],
                    listMask[layoutPosition],
                    layoutPosition
                )
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.row_mask, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return imglist.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        val item = imglist[i]
        holder.image.setImageBitmap(item)
        holder.name.text= listMask[i].categories[0].label.replaceFirstChar { it.uppercase() } + " "+(listMask[i].categories[0].score*100).toInt() +"%"
    }

}