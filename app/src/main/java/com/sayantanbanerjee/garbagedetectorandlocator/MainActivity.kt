package com.sayantanbanerjee.garbagedetectorandlocator

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

const val PICK_IMAGE = 1

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var photoHolder: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val galleryButton: Button = findViewById(R.id.galleryButton)
        photoHolder = findViewById(R.id.picView)

        galleryButton.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) {
            return
        }
        if (requestCode == PICK_IMAGE) {
            val imageBitmap: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, data?.data)
            photoHolder.setImageBitmap(imageBitmap)
        }
    }
}
