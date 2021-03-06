package com.geosoftware.victor.photovertice

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.hardware.Camera
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.Display
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.geosoftware.victor.photovertice.data.LstGalleryTask
import com.geosoftware.victor.photovertice.data.ProcessPictureTask
import com.geosoftware.victor.photovertice.ui.ImageDialogFragment
import com.geosoftware.victor.photovertice.ui.ImageDialogListener
import com.geosoftware.victor.photovertice.ui.ImageGalleryAdapter
import com.geosoftware.victor.photovertice.utils.checkGrantedPermission
import com.geosoftware.victor.photovertice.utils.getInternalDirectory
import com.geosoftware.victor.photovertice.utils.getInternalDirectoryFile
import com.geosoftware.victor.photovertice.utils.pasar_a_sexa
import kotlinx.android.synthetic.main.view_preview.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), LocationListener {
    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null
    private var display: Display? = null

    lateinit var locationManager: LocationManager

    private var latitude = 0.0
    private var longitude = 0.0
    private var altitude = 0.0

    private var galleryVisible = false


    private val mPicture = Camera.PictureCallback { data, _ ->
        val pictureFile = getInternalDirectoryFile()
        val imageDataLayoutHeight = image_data_layout.height
        val displayHeight = display?.height

        displayHeight?.let { ProcessPictureTask(data, pictureFile, imageDataLayoutHeight, it, image_data_layout) {
            val message = getString(R.string.fotografia) + " " + pictureFile.name + " " + getString(R.string.realizada_con_exito)
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            mCamera?.startPreview()
            mCamera?.reconnect()
            progress_circular.visibility = View.GONE

            LstGalleryTask(getInternalDirectory()) { imageList ->
                listView.adapter = ImageGalleryAdapter(this@MainActivity, imageList)
                showNewImageDialog(imageList.last())
                listView.visibility = View.VISIBLE
            }.execute()

        }.execute() }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT //Prevengo que la imagen se apaise!!

        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_preview)

        //------------------ Control del GPS
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val fineLocationPermission = checkGrantedPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val cameraPermission = checkGrantedPermission(Manifest.permission.CAMERA)
        val writePermission = checkGrantedPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (fineLocationPermission && cameraPermission && writePermission) {
            initializeLocation()
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    CAMERA_PERMISSION_REQUEST)
        }

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.no_gps))
            builder.setMessage(getString(R.string.no_gps_descripcion))

            builder.setPositiveButton(getString(R.string.aceptar)) { dialog, which ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }

            builder.setNegativeButton(getString(R.string.cancelar)) { dialog, which -> dialog.cancel() }

            val gpsAlertDialog = builder.create()
            gpsAlertDialog.show()
        }

        LstGalleryTask(getInternalDirectory()) { imageList ->
            listView.adapter = ImageGalleryAdapter(this@MainActivity, imageList)
        }.execute()

        //----- EVENTOS -----
        imageView.setOnClickListener {
            progress_circular.visibility = View.VISIBLE
            mCamera?.takePicture(null, null, mPicture)
        }

        listView.visibility = View.INVISIBLE
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            (listView.getItemAtPosition(position) as ImageDataModel).let { selectedImage ->
                showNewImageDialog(selectedImage)
            }
        }

        imageView2.setOnClickListener {
            if (galleryVisible) {
                listView.visibility = View.INVISIBLE
                galleryVisible = false
            } else {
                listView.visibility = View.VISIBLE
                galleryVisible = true
            }
        }
    }

    override fun onResume() {
        super.onResume()

        //------------------- Control de la camara
        display = windowManager.defaultDisplay
        mCamera = cameraInstance
        mCamera?.setDisplayOrientation(90)

        mPreview = CameraPreview(this, mCamera, display?.width ?: 0, display?.height ?: 0)
        camera_preview.addView(mPreview)

        val params = mCamera?.parameters
        params?.flashMode = Camera.Parameters.FLASH_MODE_AUTO
        params?.setRotation(90)

        try {
            mCamera?.parameters = params
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()

        if (mCamera != null) {
            mCamera?.release()
            mCamera = null
            mPreview = null
            camera_preview.removeAllViews()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.all{ it == PackageManager.PERMISSION_GRANTED }) {
            initializeLocation()
        }
    }

    override fun onLocationChanged(location: Location?) {
        val tresDecimales = DecimalFormat("0.000")
        latitude = location?.latitude ?: 0.0
        longitude = location?.longitude ?: 0.0
        altitude = location?.altitude ?: 0.0
        val timeStamp = SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.getDefault()).format(Date())

        textView.text = "${getString(R.string.latitud)}: ${pasar_a_sexa(latitude)}"
        textView3.text = getString(R.string.longitud) + ": " + pasar_a_sexa(longitude)
        textView4.text = getString(R.string.altitud) + ": " + tresDecimales.format(altitude) + " m"
        textView2.text = timeStamp
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) { }

    override fun onProviderEnabled(p0: String?) {
        Toast.makeText(applicationContext, getString(R.string.gps_activado), Toast.LENGTH_SHORT).show()
    }

    override fun onProviderDisabled(p0: String?) {
        Toast.makeText(applicationContext, getString(R.string.gps_desactivado), Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingPermission")
    private fun initializeLocation() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
        textView2.text = getString(R.string.esperando_senal)
    }

    private fun showNewImageDialog(selectedImage: ImageDataModel) {
        val dialog = ImageDialogFragment.newInstance(selectedImage.src, object : ImageDialogListener {
            override fun onImageDeleted() {
                with(listView.adapter as ImageGalleryAdapter) {
                    remove(selectedImage)
                    notifyDataSetChanged()
                }
            }
        })
        dialog.show(supportFragmentManager, "Dialog")
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 1001


        //----- METODOS
        val cameraInstance: Camera?
            get() {
                var c: Camera? = null

                try {
                    c = Camera.open()
                } catch (e: Exception) {

                }

                return c
            }
    }
}




