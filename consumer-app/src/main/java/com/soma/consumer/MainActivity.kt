package com.soma.consumer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.soma.consumer.ble.BleClient
import com.soma.consumer.shared.Protocol

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvResult: TextView
    private lateinit var btnScanQr: Button
    private lateinit var btnStartBle: Button
    private lateinit var btnStopBle: Button

    private val client by lazy { BleClient(this) }
    private var lastAmount = "10000" // مقدار نمونه؛ با QR جایگزین می‌شود

    private val blePerms = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> startBleFlow() }

    private val qrLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        val data = res.data?.getStringExtra("qr") ?: return@registerForActivityResult
        // انتظار: QR شامل amount باشد (مثلاً soma:pay?amount=12000)
        lastAmount = Regex("amount=(\\d+)").find(data)?.groupValues?.get(1) ?: lastAmount
        tvStatus.text = "مبلغ از QR: $lastAmount"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus  = findViewById(R.id.tvStatus)
        tvResult  = findViewById(R.id.tvResult)
        btnScanQr = findViewById(R.id.btnScanQr)
        btnStartBle = findViewById(R.id.btnStartBle)
        btnStopBle  = findViewById(R.id.btnStopBle)

        btnScanQr.setOnClickListener {
            // اگر QrScanner خودت را داری همان را صدا بزن؛ اینجا فقط Activity فرضی ست
            startActivity(Intent(this, QrScanner::class.java)).also {
                // اگر QrScanner مستقل نیست، از لانچر بالا استفاده کن
            }
        }

        btnStartBle.setOnClickListener {
            val perms = mutableListOf(Manifest.permission.BLUETOOTH_CONNECT)
            if (Build.VERSION.SDK_INT >= 31) perms += Manifest.permission.BLUETOOTH_SCAN
            else perms += Manifest.permission.ACCESS_FINE_LOCATION
            val need = perms.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
            if (need) blePerms.launch(perms.toTypedArray()) else startBleFlow()
        }

        btnStopBle.setOnClickListener {
            tvStatus.text = "توقف"
        }
    }

    private fun startBleFlow() {
        val txId = Protocol.newTxId()
        tvResult.text = "-"
        client.start(
            amount = lastAmount,
            txId = txId,
            onLog = { msg -> runOnUiThread { tvStatus.text = msg } }
        ) { ok, rTx ->
            runOnUiThread {
                val color = if (ok) 0xFF2E7D32.toInt() else 0xFFC62828.toInt()
                tvResult.setTextColor(color)
                tvResult.text = if (ok) "موفق ✅ | کد: $rTx" else "ناموفق ❌ | کد: $rTx"
            }
        }
    }
}
