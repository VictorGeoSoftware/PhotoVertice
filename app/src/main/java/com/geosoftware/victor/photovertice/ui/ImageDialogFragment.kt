package com.geosoftware.victor.photovertice.ui

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.FileProvider
import com.geosoftware.victor.photovertice.R
import com.geosoftware.victor.photovertice.utils.decodeSampledBitmapFromResource
import kotlinx.android.synthetic.main.image_dialog_fragment.*
import java.io.File

interface ImageDialogListener {
    fun onImageDeleted()
}

class ImageDialogFragment : AppCompatDialogFragment() {

    companion object {
        private const val IMAGE_PATH = "IMAGE_PATH"

        fun newInstance(selectedImagePath: String, listener: ImageDialogListener): ImageDialogFragment {
            val bundle = Bundle()
            bundle.putString(IMAGE_PATH, selectedImagePath)

            val fragment = ImageDialogFragment()
            fragment.listener = listener
            fragment.arguments = bundle
            return fragment
        }
    }

    var listener: ImageDialogListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.image_dialog_fragment, container)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(0))

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val imagePath = arguments?.getString(IMAGE_PATH) ?: ""
        val bmp = decodeSampledBitmapFromResource(imagePath, 640, 480)

        bmp?.let {
            imageView3.setImageBitmap(it)

            btn_share_image.setOnClickListener {
                val intentShare = Intent(Intent.ACTION_SEND)
                intentShare.type = "image/*"

                context?.let { solvedContext ->
                    intentShare.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                            solvedContext,
                            solvedContext.applicationContext?.packageName + ".provider",
                            File(imagePath)))
                    intentShare.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivity(Intent.createChooser(intentShare, "GeoPhotos"))
                }
            }

            btn_delete_image.setOnClickListener {
                File(imagePath).delete()
                listener?.onImageDeleted()
                dismiss()
            }
        }
    }
}