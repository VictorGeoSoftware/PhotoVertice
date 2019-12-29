package com.geosoftware.victor.photovertice.ui

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import com.geosoftware.victor.photovertice.ImageDataModel
import com.geosoftware.victor.photovertice.R

class ImageGalleryAdapter(
        private var contextActivity: Activity,
        private var images: List<ImageDataModel>) : ArrayAdapter<ImageDataModel>(contextActivity, R.layout.image_row, images) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: ViewHolder?

        if (convertView == null) {
            val inflater = contextActivity.layoutInflater
            convertView = inflater.inflate(R.layout.image_row, null)
            holder = ViewHolder()

            holder.imageView = convertView!!.findViewById<View>(R.id.imageView) as ImageView
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        holder.imageView!!.setImageBitmap(images[position].bmp)

        return convertView
    }

    private inner class ViewHolder {
        internal var imageView: ImageView? = null
    }
}