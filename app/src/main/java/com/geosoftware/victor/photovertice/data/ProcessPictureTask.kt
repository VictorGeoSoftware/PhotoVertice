package com.geosoftware.victor.photovertice.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.os.AsyncTask
import android.widget.LinearLayout
import com.geosoftware.victor.photovertice.utils.loadBitmapFromView
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class ProcessPictureTask(
        private val pictureData: ByteArray,
        private val pictureFile: File,
        private val imageDataLayoutHeight: Int,
        private val displayHeight: Int,
        private val dataLayout: LinearLayout,
        private val onPictureReady: () -> Unit) : AsyncTask<Void, Void, Boolean>() {

    override fun doInBackground(vararg params: Void): Boolean? {
        try {
            val fos = FileOutputStream(pictureFile.absolutePath)
            fos.write(pictureData)
            fos.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }

        //----- Añadir marco con metadatos y timestamp
        //----- Preparo imagen grande
        val image = BitmapFactory.decodeFile(pictureFile.absolutePath)
        val mutableImage = image.copy(Bitmap.Config.ARGB_8888, true)
        val matrix = Matrix()

        if (mutableImage.width < mutableImage.height) {//Imagen de pie
            matrix.postRotate(0f)
        } else { //Imagen tumbada
            matrix.postRotate(90f)
        }

        val mutableImageRotated = Bitmap.createBitmap(mutableImage, 0, 0, mutableImage.width,
                mutableImage.height, matrix, true)

        val comboImage = Canvas(mutableImageRotated)

        //----- Preparo metadatos y timestamp
        val topImage = loadBitmapFromView(dataLayout)
        val topImageHeight = imageDataLayoutHeight * mutableImageRotated.height / displayHeight

        matrix.postScale(mutableImageRotated.width.toFloat(), topImageHeight.toFloat())
        val topImageScaled = Bitmap.createScaledBitmap(topImage, mutableImageRotated.width, topImageHeight, true)

        //----- Pinto metadatos encima de imagen grande
        comboImage.drawBitmap(topImageScaled, 0f, 0f, null)

        return try {
            val fos = FileOutputStream(pictureFile.absolutePath)
            mutableImageRotated.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            fos.flush()
            fos.close()

            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    override fun onPostExecute(aVoid: Boolean) {
        super.onPostExecute(aVoid)
        onPictureReady.invoke()
    }
}