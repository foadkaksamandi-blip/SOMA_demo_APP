package com.soma.consumer.qr

import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions

class QrScanner(private val activity: AppCompatActivity) {

    private var launcher: ActivityResultLauncher<ScanOptions>
    private var onResultCallback: ((String) -> Unit)? = null
    private var onCancelCallback: (() -> Unit)? = null

    init {
        launcher = activity.registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
            if (result.contents != null) {
                onResultCallback?.invoke(result.contents!!)
            } else {
                onCancelCallback?.invoke()
            }
        }
    }

    fun startScan(onResult: (String) -> Unit, onCancel: () -> Unit = {}) {
        onResultCallback = onResult
        onCancelCallback = onCancel

        val options = ScanOptions()
            .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            .setBeepEnabled(true)
            .setPrompt("کُد را اسکن کنید")
            .setOrientationLocked(true)

        launcher.launch(options)
    }
}
