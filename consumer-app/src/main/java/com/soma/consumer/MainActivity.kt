package com.soma.consumer

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.soma.consumer.ble.BleClient
import com.soma.consumer.utils.Perms
import com.soma.consumer.utils.QRHandler

class MainActivity : AppCompatActivity() {

    private val bleClient by lazy { BleClient(this) }

    private lateinit var tvAmount: TextView
    private lateinit var tvStatus: TextView
    private lateinit var ivQR: ImageView
    private lateinit var btnScanQR: Button
    private lateinit var btnStartBLE: Button
    private lateinit var btnStopBLE: Button

    private var amount = -300000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvAmount = findViewById(R.id.tvAmount)
        tvStatus = findViewById(R.id.tvStatus)
        ivQR = findViewById(R.id.ivQR)
        btnScanQR = findViewById(R.id.btnScanQR)
        btnStartBLE = findViewById(R.id.btnStartBLE)
        btnStopBLE  = findViewById(R.id.btnStopBLE)

        tvAmount.text = amount.toString()

        // اسکن QR (اگر دکمه‌اش در لایه هست)
        btnScanQR.setOnClickListener {
            if (Perms.ensureCamera(this)) {
                // اگر الان فقط نمایش QR لازم داری، مثال ساخت QR:
                val data = "SOMA|TX|${System.currentTimeMillis()}"
                QRHandler.renderTo(ivQR, data)
                tvStatus.text = "QR ساخته شد"
            }
        }

        btnStartBLE.setOnClickListener { startBLE() }
        btnStopBLE.setOnClickListener { stopBLE() }
    }

    private fun startBLE() {
        if (!Perms.ensureBleScan(this)) {
            tvStatus.text = "مجوزهای BLE کامل نیست"
            return
        }
        tvStatus.text = "در حال جستجو برای دستگاه فروشنده..."

        bleClient.startScan(
            onFound = { msg ->
                runOnUiThread { tvStatus.text = "پیام از فروشنده: $msg" }
            },
            onStop = {
                runOnUiThread { tvStatus.text = "اسکن متوقف شد" }
            }
        )
    }

    private fun stopBLE() {
        bleClient.stopScan()
        tvStatus.text = "BLE متوقف شد"
    }
}
