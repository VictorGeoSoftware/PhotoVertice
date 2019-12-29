package com.geosoftware.victor.photovertice.utils

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

fun AppCompatActivity.checkGrantedPermission(permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(
            this,
            permission) == PackageManager.PERMISSION_GRANTED
}

fun AppCompatActivity.getInternalDirectory(): File {
    val dir = File(this.filesDir, "GeoPhoto")

    if (!dir.exists()) {
        dir.mkdir()
    }

    return dir
}

fun AppCompatActivity.getInternalDirectoryFile(): File {
    val dir = getInternalDirectory()
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    return File(dir.path + File.separator + "VER_" + timeStamp + ".jpg")
}

fun loadBitmapFromView(v: LinearLayout): Bitmap {
    val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
    val c = Canvas(b)
    v.layout(v.left, v.top, v.right, v.bottom)
    v.draw(c)
    return b
}

fun pasar_a_sexa(angulo: Double) //angulo -> angulo sexa con decimales
        : String {
    val Decimal = DecimalFormat("0.00")
    val NoDecimal = DecimalFormat("0")

    val angulo_str = java.lang.Double.toString(angulo)
    val grados = quita_puntos(angulo_str)
    val angulo_entero = java.lang.Double.parseDouble(grados)
    //Decimales
    val parte_decimal = angulo - angulo_entero
    var minutos = 0.0
    minutos = parte_decimal * 60 //Minutos
    val minutos_str = java.lang.Double.toString(minutos)
    val minutos_enteros = quita_puntos(minutos_str)
    val minutos_ent = java.lang.Double.parseDouble(minutos_enteros)

    val parte_decimal_segundos = minutos - minutos_ent
    var segundos = 0.0
    segundos = parte_decimal_segundos * 60

    return grados + "º " + NoDecimal.format(Math.abs(minutos_ent)) + "' " + Decimal.format(Math.abs(segundos)) + "''"
}

fun quita_puntos(numero: String): String {
    var lector = ""
    var entero = ""
    val caracteres = numero.length
    var i = 0
    while (i < caracteres) {
        lector = numero.substring(i, i + 1)
        if (lector.contentEquals(".") || lector.contentEquals(",")) {
            i = caracteres
        } else {
            entero += lector
        }
        i++
    }
    return entero
}