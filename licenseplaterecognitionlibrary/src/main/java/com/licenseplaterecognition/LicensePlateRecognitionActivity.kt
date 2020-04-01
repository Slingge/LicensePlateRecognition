package com.licenseplaterecognition

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_licenseplate_recognition.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.opencv.android.OpenCVLoader
import java.io.ByteArrayOutputStream

class LicensePlateRecognitionActivity : AppCompatActivity() {


    private val cameraPreview by lazy { CameraPreviews(this, viewFlipper) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_licenseplate_recognition)

        if (OpenCVLoader.initDebug()) {
            Log.e("Opencv", "opencv load_success")
        } else {
            Log.e("Opencv", "opencv can't load opencv .")
        }
        iv_lamp.setOnClickListener { FlashUtils.getInstance(cameraPreview.mCamera).switchFlash() }
    }


    override fun onResume() {
        super.onResume()
        if (PermissionUtil.ApplyPermissionAlbum(this, 0)) {
            initJniCamera()
        }
    }

    private fun initJniCamera() {
        preview_fl.removeAllViews()
        InitOpencv.initRecognizer(this)
        preview_fl.addView(cameraPreview)
        Log.e("add", "add")
    }

    override fun onPause() {
        super.onPause()
        stopPreview()
    }

    private fun stopPreview() {
        preview_fl.removeAllViews()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(plate: PlateInfo) {

        val intent = Intent()
        intent.putExtra("color", plate.plateColor)
        intent.putExtra("plateName", plate.plateName)

        var baos = ByteArrayOutputStream()
        plate.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val bitmapByte: ByteArray = baos.toByteArray()
        intent.putExtra("bitmap", bitmapByte)

        baos = ByteArrayOutputStream()
        plate.bitmaps.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val bitmapBytes: ByteArray = baos.toByteArray()
        intent.putExtra("bitmaps", bitmapBytes)

        setResult(0, intent)
        finish()
    }


    /**
     * 申请权限结果回调
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {//询问结果
                InitOpencv.initRecognizer(this)
            }
        }
    }


}
