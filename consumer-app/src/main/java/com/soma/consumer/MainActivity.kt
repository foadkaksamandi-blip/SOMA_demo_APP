package com.soma.consumer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.soma.consumer.ble.BleClient

class MainActivity : AppCompatActivity(), BleClient.Listener {

    private lateinit var statusTv: TextView
    private lateinit var resultTv: TextView
    private lateinit var startBtn: Button
    private lateinit var stopBtn: Button
    private lateinit var qrBtn: Button

    private lateinit var bleClient: BleClient

    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // بعد از اعطای مجوز دوباره تلاش کن
        bleClient.start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusTv = findViewById(R.id.tvStatus)
        resultTv = findViewById(R.id.tvResult)
        startBtn = findViewById(R.id.btnStartBle)
        stopBtn = findViewById(R.id.btnStopBle)
        qrBtn = findViewById(R.id.btnScanQr)

        bleClient = BleClient(this, this)

        startBtn.setOnClickListener {
            ensurePermsThen { bleClient.start() }
        }
        stopBtn.setOnClickListener { bleClient.stop() }

        qrBtn.setOnClickListener {
            // اگر اسکنر QR دارید، همون Activity قبلی‌تون رو صدا بزنید
            resultTv.text = "QR: شبیه‌سازی - اینجا اسکنر خودتون را باز کنید"
        }

        onStatus("آماده")
    }

    private fun ensurePermsThen(block: () -> Unit) {
        if (Build.VERSION.SDK_INT >= 31) {
            val need = arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            ).any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
            if (need) {
                permLauncher.launch(arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                ))
                return
            }
        }
        block()
    }

    // ===== BleClient.Listener =====
    override fun onStatus(msg: String) { statusTv.text = "وضعیت: $msg" }
    override fun onConnected(deviceName: String?) { resultTv.text = "اتصال به: ${deviceName ?: "-"}" }
    override fun onDisconnected() { resultTv.text = "قطع شد" }
    override fun onError(msg: String) { resultTv.text = "خطا: $msg" }
}
