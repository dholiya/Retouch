package zest.photoeditorpro.photoeditor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import zest.photoeditorpro.photoeditor.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.cardRemoveObject.setOnClickListener(this);
        binding.cardEdit.setOnClickListener(this);
        binding.cardCamera.setOnClickListener(this);
//        startActivity(Intent(this, EditImageActivity::class.java))
//

    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.card_removeObject -> {
                if (ParentActivity().checkPermissionAndOpenGallery(this)) {
                }
            }
            R.id.card_edit -> {
                if (ParentActivity().checkPermissionAndOpenGallery(this)) {
                    openGallery()
                }
            }
            R.id.card_camera -> {
                if (ParentActivity().checkPermissionAndOpenGallery(this)) {

                }
            }
        }
    }


    private fun openGallery() {
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.type = "image/*"
        getResult.launch(i)
    }


    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val i = Intent(this, EditImageActivity::class.java)
                i.putExtra("data", it.data!!.data.toString())
                startActivity(i)
            }
        }

}