// اختیاری: اگر می‌خواهی همچنان کلاس QrScanner داشته باشی، نسخه‌ی امن با context:
package com.soma.consumer.qr

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class QrScanner(
    private val activity: Activity,
    private val launcher: ActivityResultLauncher<Intent>
) {
    fun launch() {
        val options = ScanOptions()
            .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            .setPrompt("QR را مقابل دوربین بگیرید")
            .setBeepEnabled(true)
            .setOrientationLocked(false)
        val intent = ScanContract().createIntent(activity, options) // دیگر null نیست
        launcher.launch(intent)
    }
}
