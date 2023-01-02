package zest.photoeditorpro.photoeditor.data//package zest.photoeditorpro.photoeditor.data.tools

import android.content.res.Resources
import zest.photoeditorpro.photoeditor.R
import zest.photoeditorpro.photoeditor.util.Str


data class Tools(
    var id: Int,
    val name: String,
    val image: Int,
)

fun getToolList(resources: Resources): List<Tools> {
    return listOf(
        Tools(
            id = Str.CROP,
            name = resources.getString(R.string.crop),
            image = R.drawable.tool_crop_24
        ),
        Tools(
            id = Str.AUTO,
            name = resources.getString(R.string.auto),
            image = R.drawable.tool_enhance
        ),
        Tools(
            id = Str.BRUSH,
            name = resources.getString(R.string.brush),
            image = R.drawable.remove_brush
        ),
        Tools(
            id = Str.ERASE,
            name = resources.getString(R.string.erase),
            image = R.drawable.remove_erase
        ),
    );
}
