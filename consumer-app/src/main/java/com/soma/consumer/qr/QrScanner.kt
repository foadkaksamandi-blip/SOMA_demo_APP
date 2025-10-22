package com.soma.consumer.qr

import android.app.Activity
import android.content.Intent
import com.journeyapps.barcodescanner.IntentIntegrator

class QrScanner(private val activity: Activity) {

    fun startScan(onResult: (String) -> Unit, onCancel: () -> Unit) {
        val integrator = IntentIntegrator(activity)
        integrator.setOrientationLocked(false)
        integrator.setBeepEnabled(true)
        integrator.setPrompt("کد QR را اسکن کنید")
        integrator.initiateScan()

        activity.activityResultRegistry.register("qr_scan", 
            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val data: Intent? = result.data
            val contents = IntentIntegrator.parseActivityResult(result.resultCode, data)?.contents
            if (contents != null) onResult(contents) else onCancel()
        }
    }
}
