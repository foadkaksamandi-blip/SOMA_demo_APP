package shared.utils

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

object QRHandler {

    fun generate(amount: String): Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(amount, BarcodeFormat.QR_CODE, 512, 512)
        val bmp = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
        for (x in 0 until 512)
            for (y in 0 until 512)
                bmp.setPixel(x, y, if (bitMatrix[x, y]) -0x1000000 else -0x1)
        return bmp
    }
}
