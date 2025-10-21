package com.soma.merchant

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.soma.merchant.ble.BleServer

class MainActivity : AppCompatActivity() {

    private lateinit var status: TextView
    private lateinit var btnNewTx: Button
    private lateinit var btnRefund: Button
    private lateinit var btnReport: Button

    private val server by lazy { BleServer(this) }

    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> startServer() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        status = findViewById(R.id.tvStatus)
        btnNewTx = findViewById(R.id.btnPay)
        btnRefund = findViewById(R.id.btnRefund)
        btnReport = findViewById(R.id.btnReport)

        btnNewTx.setOnClickListener { ensurePermsAndStart() }
        btnRefund.setOnClickListener { status.text = "مرجوعی (نمایشی)" }
        btnReport.setOnClickListener { status.text = "گزارش (نمایشی)" }
    }

    private fun ensurePermsAndStart() {
        val perms = mutableListOf(
            Manifest.permission.BLUETOOTH_CONNECT
        )
        if (Build.VERSION.SDK_INT >= 31) {
            perms += Manifest.permission.BLUETOOTH_ADVERTISE
        } else {
            perms += Manifest.permission.ACCESS_FINE_LOCATION
        }
        val need = perms.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (need) permLauncher.launch(perms.toTypedArray()) else startServer()
    }

    private fun startServer() {
        val bt = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        if (!bt.adapter.isEnabled) {
            status.text = "بلوتوث خاموش است"
            return
        }
        server.start { log -> runOnUiThread { status.text = log } }
    }

    override fun onDestroy() {
        super.onDestroy()
        server.stop { }
    }
}
