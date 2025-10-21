package com.soma.merchant

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.soma.merchant.ble.BleServer

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var btnNewTxn: Button
    private lateinit var btnRefund: Button
    private lateinit var btnReport: Button

    private lateinit var bleServer: BleServer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        btnNewTxn = findViewById(R.id.btnNewTxn)
        btnRefund = findViewById(R.id.btnRefund)
        btnReport = findViewById(R.id.btnReport)

        bleServer = BleServer(this) { status ->
            tvStatus.text = "وضعیت: $status"
        }

        btnNewTxn.setOnClickListener {
            tvStatus.text = "وضعیت: شروع تراکنش"
            bleServer.startAdvertising()
        }

        // دکمه‌های دموی غیرفعال – فقط ظاهر
        btnRefund.setOnClickListener { /* TODO: demo */ }
        btnReport.setOnClickListener { /* TODO: demo */ }
    }

    override fun onDestroy() {
        super.onDestroy()
        bleServer.stopAdvertising()
    }
}
