package com.soma.consumer.qr

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

/**
 * QrScanner – یک wrapper ساده روی ZXing که از Activity Result API استفاده می‌کند
 */
class QrScanner(
    private val launcher: ActivityResultLauncher<Intent>,
    private val onResult: (String?) -> Unit
) {
    fun launch() {
        val options = ScanOptions()
            .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            .setPrompt("QR را مقابل دوربین بگیرید")
            .setBeepEnabled(true)
            .setOrientationLocked(false)
        launcher.launch(ScanContract().createIntent(null, options))
    }

    companion object {
        fun parseResult(resultCode: Int, data: Intent?): String? {
            val result = ScanContract().parseResult(resultCode, data)
            return result?.contents
        }
    }
}
