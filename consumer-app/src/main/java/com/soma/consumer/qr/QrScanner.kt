package com.soma.consumer.qr

import android.app.Activity
import androidx.activity.result.ActivityResultCaller
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class QrScanner(
    caller: ActivityResultCaller,
    private val onResult: (String?) -> Unit
) {
    private val launcher = caller.registerForActivityResult(ScanContract()) { result ->
        onResult(result.contents)
    }

    fun start(activity: Activity) {
        val options = ScanOptions()
            .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            .setPrompt("QR را داخل کادر قرار دهید")
            .setBeepEnabled(false)
            .setOrientationLocked(true)
        launcher.launch(options)
    }
}
