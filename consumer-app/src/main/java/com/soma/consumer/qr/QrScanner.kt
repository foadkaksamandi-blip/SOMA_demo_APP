package com.soma.consumer.qr

import android.content.Context
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import android.graphics.Bitmap

class QrScanner {
    fun decode(bitmap: Bitmap): String? {
        val width = bitmap.width
        val height = bitmap.height
        val intArray = IntArray(width * height)
        bitmap.getPixels(intArray, 0, width, 0, 0, width, height)
        val source = RGBLuminanceSource(width, height, intArray)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        return try {
            MultiFormatReader().decode(binaryBitmap).text
        } catch (e: Exception) {
            null
        }
    }
}
