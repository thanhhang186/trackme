package com.khtn.trackme.utils

import android.R
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory


/**
 * Created by NguyenHang on 11/25/2020.
 */

object Utils {
    fun formatDouble(distance: Double): String {
        return String.format("%.2f", distance)
    }

    fun formatDuration(duration: Long): String {
        return String.format("%d:%02d:%02d", duration / 3600, (duration % 3600) / 60, (duration % 60))
    }

    fun bitmapDescriptorFromVector(context: Context, @DrawableRes vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }
}