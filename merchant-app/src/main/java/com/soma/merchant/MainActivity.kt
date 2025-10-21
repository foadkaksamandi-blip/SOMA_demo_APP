package com.soma.merchant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.soma.merchant.ble.BleServer

class MainActivity : AppCompatActivity(), BleServer.Listener {

    private lateinit var statusTv: TextView
    private lateinit var newTxnBtn: Button
    private lateinit var refundBtn: Button
    private lateinit var reportBtn: Button

    private lateinit var bleServer: BleServer

    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> bleServer.start() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusTv = findViewById(R.id.tvStatus)
        newTxnBtn = findViewById(R.id.btnNewTxn)
        refundBtn = findViewById(R.id.btnRefund)
        reportBtn = findViewById(R.id.btnReport)

        bleServer = BleServer(this, this)

        newTxnBtn.setOnClickListener {
            ensurePermsThen { bleServer.start() }
        }
        // برای دمو دکمه‌های دیگر کاری نمی‌کنند
        refundBtn.isEnabled = false
        reportBtn.isEnabled = false

        onStatus("آماده")
    }

    private fun ensurePermsThen(block: () -> Unit) {
        if (Build.VERSION.SDK_INT >= 31) {
            val need = arrayOf(
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT
            ).any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
            if (need) {
                permLauncher.launch(arrayOf(
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT
                ))
                return
            }
        }
        block()
    }

    // ===== BleServer.Listener =====
    override fun onStatus(msg: String) { statusTv.text = "وضعیت: $msg" }
    override fun onClientConnected(addr: String?) { statusTv.text = "کلاینت وصل شد: ${addr ?: "-"}" }
    override fun onClientDisconnected() { statusTv.text = "کلاینت قطع شد" }
    override fun onError(msg: String) { statusTv.text = "خطا: $msg" }

    override fun onDestroy() {
        super.onDestroy()
        bleServer.stop()
    }
}
