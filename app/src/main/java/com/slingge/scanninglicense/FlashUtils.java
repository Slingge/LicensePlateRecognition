package com.slingge.scanninglicense;


import android.hardware.Camera;

/**
 * Created by Slingge on 2019/12/11
 */
public class FlashUtils {

    private static FlashUtils utils;
    public static Camera cameras;
    private static boolean mIsOpen = true;

    //使用单例模式在这里初始化相机
    public static FlashUtils getInstance(Camera camera) {
        if (utils == null) {
            utils = new FlashUtils();
        }
        cameras=camera;
        return utils;
    }

    //参考二维码工具的闪光灯
    public void switchFlash() {
        try {
            Camera.Parameters parameters = cameras.getParameters();
            if (mIsOpen) {
                if (parameters.getFlashMode().equals("torch")) {
                    return;
                } else {
                    parameters.setFlashMode("torch");
                }
            } else {
                if (parameters.getFlashMode().equals("off")) {
                    return;
                } else {
                    parameters.setFlashMode("off");
                }
            }
            cameras.setParameters(parameters);
        } catch (Exception e) {
            finishFlashUtils();
        }
        mIsOpen = !mIsOpen;
    }

    //页面销毁的时候调用此方法
    public void finishFlashUtils() {
        if (cameras != null) {
            cameras.stopPreview();
            cameras.release();
        }
        cameras = null;
    }

}
