package com.geosoftware.victor.photovertice.data

import android.os.AsyncTask
import com.geosoftware.victor.photovertice.ImageDataModel
import com.geosoftware.victor.photovertice.utils.decodeSampledBitmapFromResource
import java.io.File

class LstGalleryTask(
        private val directory: File,
        private val onListReady: (List<ImageDataModel>) -> Unit) : AsyncTask<Void, Void, List<ImageDataModel>>() {

    override fun doInBackground(vararg params: Void): List<ImageDataModel>? {
        val bitmaps = ArrayList<ImageDataModel>()
        val imageList = directory.listFiles()

        imageList?.map {
            val b = decodeSampledBitmapFromResource(it.absolutePath, 64, 32)
            bitmaps.add(ImageDataModel(b, it.absolutePath))
        }

        return bitmaps
    }

    override fun onPostExecute(list: List<ImageDataModel>) {
        super.onPostExecute(list)
        onListReady.invoke(list)
    }
}