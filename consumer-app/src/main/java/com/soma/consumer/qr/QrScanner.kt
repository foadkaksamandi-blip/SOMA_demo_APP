package com.soma.consumer.qr

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.journeyapps.barcodescanner.IntentIntegrator

class QrScanner(private val activity: Activity) {

    private val launcher = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res: ActivityResult ->
        val data: Intent? = res.data
        val result = IntentIntegrator.parseActivityResult(res.resultCode, data)
        if (result != null && result.contents != null) {
            onResultCallback?.invoke(result.contents!!)
        } else {
            onCancelCallback?.invoke()
        }
        onResultCallback = null
        onCancelCallback = null
    }

    private var onResultCallback: ((String) -> Unit)? = null
    private var onCancelCallback: (() -> Unit)? = null

    fun startScan(
        onResult: (String) -> Unit,
        onCancel: () -> Unit
    ) {
        onResultCallback = onResult
        onCancelCallback = onCancel

        val integrator = IntentIntegrator(activity).apply {
            setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            setPrompt("QR را اسکن کنید")
            setBeepEnabled(false)
            setOrientationLocked(true)
        }
        val intent = integrator.createScanIntent()
        launcher.launch(intent)
    }
}
