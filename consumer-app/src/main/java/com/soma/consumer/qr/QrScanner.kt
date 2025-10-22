package com.soma.consumer.qr

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.journeyapps.barcodescanner.IntentIntegrator

/**
 * ساده‌ترین اسکنر QR با ZXing-Embedded.
 * امضا دقیقاً مطابق استفاده در MainActivity:
 *   startScan(onResult = { ... }, onCancel = { ... })
 */
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
        // مصرف و آزادسازی
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
            // از createScanIntent استفاده می‌کنیم تا با Activity Result Launcher کار کند
        }
        val intent = integrator.createScanIntent()
        launcher.launch(intent)
    }
}
