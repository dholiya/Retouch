package zest.photoeditorpro.photoeditor.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import zest.photoeditorpro.photoeditor.R
import zest.photoeditorpro.photoeditor.data.Tools

class ToolsAdapter(
    private val onToolSelected: OnToolSelected,
    val toolList: List<Tools>,
    val context: Context,
    val isToolSelected: Boolean,
) :
    RecyclerView.Adapter<ToolsAdapter.ViewHolder>() {


    interface OnToolSelected {
        fun onToolSelected(tools: Tools, tool:LinearLayoutCompat, layoutPosition: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.image)
        val name: TextView = itemView.findViewById(R.id.name)
        val tool: LinearLayoutCompat = itemView.findViewById(R.id.tool)

        init {
            itemView.setOnClickListener {
                onToolSelected.onToolSelected(
                    toolList[layoutPosition],
                    tool,
                    layoutPosition
                )
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.row_common, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return toolList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        val item = toolList[i]
        holder.image.setImageResource(item.image)
        holder.name.text = item.name
        holder.tool.setBackgroundColor(ContextCompat.getColor(context, R.color.trans))

    }

}