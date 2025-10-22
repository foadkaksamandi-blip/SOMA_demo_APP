package com.soma.consumer.qr

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class QRScanner(private val activity: Activity) {

    private var launcher: ActivityResultLauncher<ScanOptions>? = null

    /**
     * باید از Activity فراخوانی شود (مثلاً در onCreate) تا launcher ثبت شود.
     */
    fun register(
        onResult: (String) -> Unit,
        onCancel: () -> Unit = {}
    ) {
        launcher = activity.registerForActivityResult(ScanContract()) { result ->
            if (result != null && !result.contents.isNullOrEmpty()) {
                onResult(result.contents!!)
            } else {
                onCancel()
            }
        }
    }

    fun startScan(
        prompt: String = "لطفاً QR را اسکن کنید…",
        beep: Boolean = false
    ) {
        val opts = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt(prompt)
            setBeepEnabled(beep)
            setOrientationLocked(true)
        }
        launcher?.launch(opts)
            ?: throw IllegalStateException("QRScanner.register() باید قبل از startScan صدا زده شود.")
    }
}
