package com.slingge.scanninglicense

import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.opencv.android.OpenCVLoader

class MainActivity : AppCompatActivity() {


    private val cameraPreview by lazy { CameraPreviews(this, viewFlipper) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_main)

        if (OpenCVLoader.initDebug()) {
            Log.e("Opencv", "opencv load_success")
        } else {
            Log.e("Opencv", "opencv can't load opencv .")
        }

        InitOpencv.initRecognizer(this)
        iv_lamp.setOnClickListener { FlashUtils.getInstance(cameraPreview.mCamera).switchFlash() }
    }


    override fun onResume() {
        super.onResume()
        preview_fl.addView(cameraPreview)
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
        images.setImageBitmap(plate.bitmaps)
        plate_tv.text = plate.plateName.toString() + "；颜色：" + plate.plateColor
        image.setImageBitmap(plate.bitmap)
        //        stopPreview();
    }

}
