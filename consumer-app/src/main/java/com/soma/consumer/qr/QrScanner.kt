package com.soma.consumer.qr

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.IntentIntegrator
import com.journeyapps.barcodescanner.IntentResult

class QRScanner(private val activity: Activity, private val onResult: (String) -> Unit) {

    fun startScan() {
        try {
            val integrator = IntentIntegrator(activity)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            integrator.setPrompt("کد را روبه‌روی دوربین قرار دهید")
            integrator.setCameraId(0)
            integrator.setBeepEnabled(false)
            integrator.setBarcodeImageEnabled(false)
            integrator.setCaptureActivity(CaptureActivity::class.java)
            integrator.initiateScan()
        } catch (e: Exception) {
            Toast.makeText(activity, "خطا در شروع اسکن QR", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            onResult(result.contents)
        } else {
            Toast.makeText(activity, "کدی یافت نشد", Toast.LENGTH_SHORT).show()
        }
    }
}
