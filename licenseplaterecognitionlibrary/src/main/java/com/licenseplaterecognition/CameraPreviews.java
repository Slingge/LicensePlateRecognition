package com.licenseplaterecognition;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;


import org.greenrobot.eventbus.EventBus;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author by hs-johnny
 * Created on 2019/6/17
 */
public class CameraPreviews extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private static final String TAG = "CameraPreview";
    public Camera mCamera;
    private SurfaceHolder mHolder;
    public long handle;
    private byte[] lock = new byte[0];
    private Paint mPaint;
    private float oldDist = 1f;
    private Activity context;
    /**
     * 停止识别
     */
    private boolean isStopReg;

    private ViewFinderView viewFinderView;

    public CameraPreviews(Activity context, ViewFinderView viewFinderView) {
        super(context);
        this.context = context;
        this.viewFinderView = viewFinderView;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(2);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(context.getResources().getColor(R.color.colorAccent));
    }

    public Camera getCameraInstance() {
        if (mCamera == null) {
            try {
                CameraHandlerThread mThread = new CameraHandlerThread("camera thread");
                synchronized (mThread) {
                    mThread.openCamera();
                }
            } catch (Exception e) {
                Log.e(TAG, "camera is not available");
            }
        }
        return mCamera;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = getCameraInstance();
        mCamera.setPreviewCallback(this);

        try {
            //摄像头画面显示在Surface上
            if (mCamera != null) {
                Camera.Parameters parameters = mCamera.getParameters();
                List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
                int[] a = new int[sizes.size()];
                int[] b = new int[sizes.size()];
                for (int i = 0; i < sizes.size(); i++) {
                    int supportH = sizes.get(i).height;
                    int supportW = sizes.get(i).width;
                    a[i] = Math.abs(supportW - ViewFinderView.screenHeight);
                    b[i] = Math.abs(supportH - ViewFinderView.screenWidth);
                    Log.d("TEST", "supportW:" + supportW + "supportH:" + supportH);
                }
                int minW = 0, minA = a[0];
                for (int i = 0; i < a.length; i++) {
                    if (a[i] <= minA) {
                        minW = i;
                        minA = a[i];
                    }
                }
                int minH = 0, minB = b[0];
                for (int i = 0; i < b.length; i++) {
                    if (b[i] < minB) {
                        minH = i;
                        minB = b[i];
                    }
                }
                Log.d("TEST", "result=" + sizes.get(minW).width + "x" + sizes.get(minH).height);
//                List<Integer> list = parameters.getSupportedPreviewFrameRates();
                parameters.setPreviewSize(sizes.get(minW).width, sizes.get(minH).height); // 设置预览图像大小
//                parameters.setPreviewFrameRate(list.get(list.size() - 1));
                mCamera.setParameters(parameters);
//                mCamera.setDisplayOrientation(90);
//                mCamera.startPreview();
            }
        } catch (Exception e) {
            if (mCamera != null)
                mCamera.release();
            mCamera = null;
        }

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            setPreviewFocus(mCamera);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        int rotation = getDisplayOrientation();
        mCamera.setDisplayOrientation(rotation);
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setRotation(rotation);
        mCamera.setParameters(parameters);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder.removeCallback(this);
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    public int getDisplayOrientation() {
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
        int result = (info.orientation - degrees + 360) % 360;
        return result;
    }

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        synchronized (lock) {
            Log.e(TAG, "间隔时间:  ----time: " + System.currentTimeMillis());
            //处理data
            Camera.Size previewSize = camera.getParameters().getPreviewSize();
            BitmapFactory.Options newOpts = new BitmapFactory.Options();
            newOpts.inJustDecodeBounds = true;
            YuvImage yuvimage = new YuvImage(
                    data,
                    ImageFormat.NV21,
                    previewSize.width,
                    previewSize.height,
                    null);
//            Log.e("bitmap", "height = " + yuvimage.getHeight() + " width = " + yuvimage.getWidth());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 100, baos);
            byte[] rawImage = baos.toByteArray();
            //将rawImage转换成bitmap
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap bitmap = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length, options);
            Bitmap bitmaps = rotateBitmap(cutRotateBitmapTest(bitmap));
            float dp_asp = 8 / 10.f;
//        imgv.setImageBitmap(bmp);
            Mat mat_src = new Mat(bitmaps.getWidth(), bitmaps.getHeight(), CvType.CV_8UC4);

            float new_w = bitmaps.getWidth() * dp_asp;
            float new_h = bitmaps.getHeight() * dp_asp;
            Size sz = new Size(new_w, new_h);
            Utils.bitmapToMat(bitmaps, mat_src);
            Imgproc.resize(mat_src, mat_src, sz);
            PlateInfo result = PlateRecognition.INSTANCE.PlateInfoRecognization(mat_src.getNativeObjAddr(), InitOpencv.INSTANCE.getHandle());

            result.bitmaps = bitmaps;
            bitmap.recycle();

//            Log.e(TAG, "onPreviewFrame: " + result.plateName + "----time: " + System.currentTimeMillis());
            if (!isStopReg && result != null && !TextUtils.isEmpty(result.plateName)) {
                isStopReg = true;
                Log.e(TAG, "camera is not available");
                sendPlate(result);
            }
        }
    }


    private void sendPlate(PlateInfo plate) {
        EventBus.getDefault().post(plate);
    }

    private Bitmap rotateBitmap(Bitmap bmp) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        if (heightPixels == 0) {
            getPixelsHieigh();
            getStatusBarHeight();
        }
        Bitmap rotatedBitMap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        return rotatedBitMap;
    }

    private Bitmap cutRotateBitmap(Bitmap bmp) {
        Matrix matrix = new Matrix();
        if (heightPixels == 0) {
            getPixelsHieigh();
            getStatusBarHeight();
        }
        Bitmap rotatedBitMap = Bitmap.createBitmap(bmp, 0, (bmp.getHeight() / 2) - (bmp.getHeight() / 6), bmp.getWidth(), bmp.getHeight() / 4, matrix, true);
        return rotatedBitMap;
    }

    Matrix matrix = new Matrix();

    private Bitmap cutRotateBitmapTest(Bitmap bmp) {
        Bitmap rotatedBitMap = Bitmap.createBitmap(bmp, ViewFinderView.center.top,
                ViewFinderView.center.left, ViewFinderView.center.height(), ViewFinderView.center.width()
                , matrix, true);
        return rotatedBitMap;
    }

    private void openCameraOriginal() {
        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            Log.e(TAG, "camera is not available");
        }
    }

    private class CameraHandlerThread extends HandlerThread {
        Handler handler;

        public CameraHandlerThread(String name) {
            super(name);
            start();
            handler = new Handler(getLooper());
        }

        synchronized void notifyCameraOpened() {
            notify();
        }

        void openCamera() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    openCameraOriginal();
                    notifyCameraOpened();
                }
            });
            try {
                wait();
            } catch (Exception e) {
                Log.e(TAG, "wait was interrupted");
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = getFingerSpacing(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float newDist = getFingerSpacing(event);
                    if (newDist > oldDist) {
                        handleZoom(true, mCamera);
                    } else if (newDist < oldDist) {
                        handleZoom(false, mCamera);
                    }
                    oldDist = newDist;
                    break;
            }
        }
        return true;
    }

    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void handleZoom(boolean isZoomIn, Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        if (parameters.isZoomSupported()) {
            int maxZoom = parameters.getMaxZoom();
            int zoom = (int) (parameters.getZoom());
            if (isZoomIn && zoom < maxZoom) {
                zoom++;
            } else if (zoom > 0) {
                zoom--;
            }
            parameters.setZoom(zoom);
            camera.setParameters(parameters);
        } else {
            Log.e(TAG, "handleZoom: " + "the device is not support zoom");
        }
    }

    private void setPreviewFocus(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        List<String> focusList = parameters.getSupportedFocusModes();
        if (focusList.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        camera.setParameters(parameters);
    }


    private Bitmap ScreenshotBitmap(int desWidth, int desHeight, Bitmap originBitmap) {

//        Log.e("bitmap", "height = " + desHeight + " width = "
//                + desWidth);

//        Log.e("bitmap", "height = " + originBitmap.getHeight() + " width = "
//                + originBitmap.getWidth());
        int width = desWidth;
        int height = desHeight;
        if (originBitmap.getWidth() <= desWidth) {
            width = originBitmap.getWidth();
        }
        if (originBitmap.getHeight() <= desHeight) {
            height = originBitmap.getHeight();
        }

//        Log.e("bitmap", "height = " + height + " width = " + width);

        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap desBitmap = Bitmap.createBitmap(originBitmap, viewFinderView.center.top, viewFinderView.center.left, width,
                height, matrix, true);
//        Log.e("bitmap", "height = " + desBitmap.getHeight() + " width = "
//                + desBitmap.getWidth());
        return desBitmap;
    }


    /**
     * 获取状态栏高度
     *
     * @param context
     * @return
     */
    static Resources resources;

    public void getStatusBarHeight() {
        if (resources == null) {
            resources = context.getResources();
        }
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        StatusBarHeight = resources.getDimensionPixelSize(resourceId);
    }

    private int heightPixels = 0, StatusBarHeight = 0;

    private void getPixelsHieigh() {
        Display defaultDisplay = context.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        heightPixels = point.y;
    }

}
