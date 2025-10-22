package com.soma.consumer.qr

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class QRScanner(private val activity: Activity, private val onResult: (String) -> Unit) {

    private val launcher = activity.registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) onResult(result.contents)
        else Toast.makeText(activity, "کدی یافت نشد", Toast.LENGTH_SHORT).show()
    }

    fun startScan() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("کد را روبه‌روی دوربین قرار دهید")
        options.setCameraId(0)
        options.setBeepEnabled(false)
        options.setOrientationLocked(true)
        options.setCaptureActivity(CaptureActivity::class.java)
        launcher.launch(options)
    }
}
