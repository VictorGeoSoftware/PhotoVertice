package com.geosoftware.victor.photovertice.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory

fun decodeSampledBitmapFromResource(filePath: String?, reqWidth: Int, reqHeight: Int): Bitmap? {
    filePath?.let {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(filePath, options)
    } ?: run { return null }
}

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    var inSampleSize = 1

    if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
        val halfHeight = options.outHeight / 2
        val halfWidth = options.outWidth / 2
        while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}