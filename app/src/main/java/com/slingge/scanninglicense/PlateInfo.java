package com.slingge.scanninglicense;

import android.graphics.Bitmap;

/**
 * @author liuxuhui
 * @date 2019/6/20
 */
public class PlateInfo {

    /**
     * 车牌号
     */
    public String plateName;

    /**
     * 车牌号图片
     */
    public Bitmap bitmap;

    public Bitmap bitmaps;//识别原图

    public String plateColor;//车牌颜色

    public PlateInfo() {
    }

    public PlateInfo(String plateName, Bitmap bitmap, Bitmap bitmaps) {
        this.plateName = plateName;
        this.bitmap = bitmap;
        this.bitmaps=bitmaps;
    }
}
