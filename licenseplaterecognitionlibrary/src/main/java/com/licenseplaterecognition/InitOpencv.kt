package com.licenseplaterecognition

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream

/**
 * Created by Slingge on 2019/12/13
 */
object InitOpencv{


     var handle: Long = 0

    fun initRecognizer(context: Context) {
        val assetPath = "pr"
        val sdcardPath = Environment.getExternalStorageDirectory()
            .toString() + File.separator + assetPath
        copyFilesFromAssets(context, assetPath, sdcardPath)
        val cascade_filename = (sdcardPath
                + File.separator + "cascade.xml")
        val finemapping_prototxt = (sdcardPath
                + File.separator + "HorizonalFinemapping.prototxt")
        val finemapping_caffemodel = (sdcardPath
                + File.separator + "HorizonalFinemapping.caffemodel")
        val segmentation_prototxt = (sdcardPath
                + File.separator + "Segmentation.prototxt")
        val segmentation_caffemodel = (sdcardPath
                + File.separator + "Segmentation.caffemodel")
        val character_prototxt = (sdcardPath
                + File.separator + "CharacterRecognization.prototxt")
        val character_caffemodel = (sdcardPath
                + File.separator + "CharacterRecognization.caffemodel")
        val segmentationfree_prototxt = (sdcardPath
                + File.separator + "SegmenationFree-Inception.prototxt")
        val segmentationfree_caffemodel = (sdcardPath
                + File.separator + "SegmenationFree-Inception.caffemodel")
        handle = PlateRecognition.InitPlateRecognizer(
            cascade_filename,
            finemapping_prototxt, finemapping_caffemodel,
            segmentation_prototxt, segmentation_caffemodel,
            character_prototxt, character_caffemodel,
            segmentationfree_prototxt, segmentationfree_caffemodel
        )
    }


    fun copyFilesFromAssets(
        context: Context,
        oldPath: String,
        newPath: String
    ) {
        try {
            val fileNames = context.assets.list(oldPath)
            if (fileNames!!.size > 0) { // directory
                val file = File(newPath)
                if (!file.mkdir()) {
                    Log.e("mkdir", "can't make folder")
                }
                //                    return false;                // copy recursively
                for (fileName in fileNames) {
                    copyFilesFromAssets(
                        context, "$oldPath/$fileName",
                        "$newPath/$fileName"
                    )
                }
            } else { // file
                val `is` = context.assets.open(oldPath)
                val fos = FileOutputStream(File(newPath))
                val buffer = ByteArray(1024)
                var byteCount: Int
                while (`is`.read(buffer).also { byteCount = it } != -1) {
                    fos.write(buffer, 0, byteCount)
                }
                fos.flush()
                `is`.close()
                fos.close()
            }
        } catch (e: Exception) { // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }


}