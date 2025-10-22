package com.soma.consumer.qr

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class QRScanner(
    activity: AppCompatActivity,
    private val onResult: (String) -> Unit
) {
    private val launcher =
        activity.registerForActivityResult(ScanContract()) { result ->
            if (result != null && result.contents != null) {
                onResult(result.contents)
            } else {
                onResult("لغو شد")
            }
        }

    fun startScan() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setBeepEnabled(false)
            setPrompt("QR را اسکن کنید")
        }
        launcher.launch(options)
    }
}
