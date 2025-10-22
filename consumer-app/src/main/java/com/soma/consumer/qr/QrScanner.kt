package com.soma.consumer.qr

import android.app.Activity
import android.content.Intent
import com.google.zxing.integration.android.IntentIntegrator

class QRScanner(
    private val activity: Activity,
    private val onResult: (String) -> Unit
) {

    private val integrator = IntentIntegrator(activity).apply {
        setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        setPrompt("اسکن QR کد را انجام دهید")
        setCameraId(0)
        setBeepEnabled(true)
        setBarcodeImageEnabled(false)
    }

    fun startScan() {
        integrator.initiateScan()
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        result?.contents?.let { onResult(it) }
    }
}
