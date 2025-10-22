package com.soma.consumer.qr

import android.widget.Toast
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.zxing.integration.android.IntentIntegrator

/**
 * اسکنر QR ساده که با Activity Result API کار می‌کند
 * و نتیجه را از طریق کال‌بک‌ها برمی‌گرداند.
 *
 * استفاده در MainActivity:
 *   private lateinit var qr: QrScanner
 *   override fun onCreate(...) {
 *       qr = QrScanner(this)
 *       btnQR.setOnClickListener {
 *           qr.startScan(
 *               onResult = { content -> tvResult.text = content },
 *               onCancel = { Toast.makeText(this, "لغو شد", Toast.LENGTH_SHORT).show() }
 *           )
 *       }
 *   }
 */
class QrScanner(private val activity: ComponentActivity) {

    private var onResultCb: ((String) -> Unit)? = null
    private var onCancelCb: (() -> Unit)? = null

    private val launcher = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val parsed = IntentIntegrator.parseActivityResult(result.resultCode, result.data)
        if (parsed != null && parsed.contents != null) {
            Log.d("QR", "Scan success: ${parsed.contents}")
            Toast.makeText(activity, "نتیجه QR: ${parsed.contents}", Toast.LENGTH_SHORT).show()
            onResultCb?.invoke(parsed.contents!!)
        } else {
            Log.d("QR", "Scan canceled or empty")
            Toast.makeText(activity, "اسکن لغو/بی‌نتیجه", Toast.LENGTH_SHORT).show()
            onCancelCb?.invoke()
        }
    }

    /** شروع اسکن */
    fun startScan(onResult: (String) -> Unit, onCancel: (() -> Unit)? = null) {
        onResultCb = onResult
        onCancelCb = onCancel

        val integrator = IntentIntegrator(activity).apply {
            setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            setBeepEnabled(true)
            setOrientationLocked(false)
            setPrompt("کد QR را مقابل دوربین قرار دهید")
        }

        // Intent آماده برای لانچر
        val intent = integrator.createScanIntent()
        Log.d("QR", "Launching scanner intent…")
        launcher.launch(intent)
    }
}
