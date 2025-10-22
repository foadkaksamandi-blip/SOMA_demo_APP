package com.soma.consumer.qr

import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class QrScanner(private val activity: AppCompatActivity) {

    private var onResult: ((String) -> Unit)? = null
    private var onCancel: (() -> Unit)? = null

    private val launcher = activity.registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            onResult?.invoke(result.contents!!)
        } else {
            onCancel?.invoke()
        }
    }

    fun startScan(onResult: (String) -> Unit, onCancel: () -> Unit) {
        this.onResult = onResult
        this.onCancel = onCancel

        val opts = ScanOptions()
            .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            .setBeepEnabled(true)
            .setPrompt("لطفاً QR را در کادر قرار دهید")
            .setOrientationLocked(true)

        launcher.launch(opts)
    }
}
