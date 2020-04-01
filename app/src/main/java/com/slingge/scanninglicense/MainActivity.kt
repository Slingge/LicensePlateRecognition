package com.slingge.scanninglicense

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.licenseplaterecognition.LicensePlateRecognitionActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn.setOnClickListener {
            val intent = Intent(this, LicensePlateRecognitionActivity::class.java)
            startActivityForResult(intent, 0)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let {
            val color = data.getStringExtra("color")
            val plateName = data.getStringExtra("plateName")

            plate_tv.text = "车牌号：$plateName；颜色：$color"

            val bis = data.getByteArrayExtra("bitmap")
            val bitmap: Bitmap = BitmapFactory.decodeByteArray(bis, 0, bis.size)

            val biss = data.getByteArrayExtra("bitmaps")
            val bitmaps: Bitmap = BitmapFactory.decodeByteArray(biss, 0, biss.size)
            images.setImageBitmap(bitmaps)
            image.setImageBitmap(bitmap)
        }
    }


}
