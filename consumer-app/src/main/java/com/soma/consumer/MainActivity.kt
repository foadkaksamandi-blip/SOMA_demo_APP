package com.soma.consumer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.soma.consumer.ble.BleClient
import com.soma.consumer.qr.QrScanner

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvResult: TextView
    private lateinit var btnScanQr: Button
    private lateinit var btnStartBle: Button
    private lateinit var btnStopBle: Button

    private lateinit var bleClient: BleClient
    private lateinit var qrScanner: QrScanner

    private val qrLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        val text = QrScanner.parseResult(res.resultCode, res.data)
        tvResult.text = if (text.isNullOrEmpty()) "نتیجه: -" else "نتیجه: $text"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        tvResult = findViewById(R.id.tvResult)
        btnScanQr = findViewById(R.id.btnScanQr)
        btnStartBle = findViewById(R.id.btnStartBle)
        btnStopBle = findViewById(R.id.btnStopBle)

        bleClient = BleClient(this) { status -> tvStatus.text = "وضعیت: $status" }
        qrScanner = QrScanner(qrLauncher) { /* handled in launcher */ }

        btnScanQr.setOnClickListener { qrScanner.launch() }
        btnStartBle.setOnClickListener { bleClient.start() }
        btnStopBle.setOnClickListener { bleClient.stop() }
    }
}
