package com.slingge.scanninglicense

/**
 * 一直项目需要修改CPP文件夹下hyperlpr下javaWarpper.cpp文件包名、用到的实体类路径名
 * Created by yujinke on 24/10/2017.
 */
object PlateRecognition {
    external fun InitPlateRecognizer(
        casacde_detection: String?,
        finemapping_prototxt: String?, finemapping_caffemodel: String?,
        segmentation_prototxt: String?, segmentation_caffemodel: String?,
        charRecognization_proto: String?, charRecognization_caffemodel: String?,
        segmentationfree_proto: String?, segmentationfree_caffemodel: String?
    ): Long

    external fun ReleasePlateRecognizer(`object`: Long)
    external fun SimpleRecognization(inputMat: Long, `object`: Long): String?
    external fun PlateInfoRecognization(inputMat: Long, `object`: Long): PlateInfo?

    init {
        System.loadLibrary("hyperlpr")
    }
}