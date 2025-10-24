package com.soma.merchant

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.soma.merchant.ble.BLEPeripheralService
import com.soma.merchant.utils.Perms
import com.soma.merchant.utils.QRHandler

class MainActivity : AppCompatActivity() {

    private val bleService by lazy { BLEPeripheralService() }

    private lateinit var tvAmount: TextView
    private lateinit var tvStatus: TextView
    private lateinit var ivQR: ImageView
    private lateinit var btnGenerateQR: Button
    private lateinit var btnStartBLE: Button
    private lateinit var btnStopBLE: Button

    private var amount = 200000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvAmount = findViewById(R.id.tvAmount)
        tvStatus = findViewById(R.id.tvStatus)
        ivQR = findViewById(R.id.ivQR)
        btnGenerateQR = findViewById(R.id.btnGenerateQR)
        btnStartBLE = findViewById(R.id.btnStartBLE)
        btnStopBLE = findViewById(R.id.btnStopBLE)

        tvAmount.text = amount.toString()

        btnGenerateQR.setOnClickListener { generateQR() }
        btnStartBLE.setOnClickListener { startBLE() }
        btnStopBLE.setOnClickListener { stopBLE() }
    }

    private fun generateQR() {
        val payload = "SOMA|TX|$amount|${System.currentTimeMillis()}"
        QRHandler.renderTo(ivQR, payload)
        tvStatus.text = "QR ساخته شد"
    }

    private fun startBLE() {
        if (!Perms.ensureBleAdvertise(this)) {
            tvStatus.text = "مجوزهای BLE کامل نیست"
            return
        }
        val payload = "SOMA|TX|$amount".toByteArray()
        bleService.startAdvertising(
            context = this,
            payload = payload,
            onStart = { runOnUiThread { tvStatus.text = "BLE فعال و در حال انتشار است" } },
            onFail = { code -> runOnUiThread { tvStatus.text = "خطای BLE: $code" } }
        )
    }

    private fun stopBLE() {
        bleService.stopAdvertising()
        tvStatus.text = "BLE متوقف شد"
    }
}
