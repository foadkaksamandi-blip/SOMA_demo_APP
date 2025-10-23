package com.soma.merchant

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.soma.merchant.ble.BlePeripheralService

class MainActivity : AppCompatActivity() {

    private val bleService by lazy { BlePeripheralService() }

    private lateinit var tvAmount: TextView
    private lateinit var tvStatus: TextView
    private lateinit var ivQR: ImageView
    private lateinit var btnGenerateQR: Button
    private lateinit var btnStartBLE: Button
    private lateinit var btnStopBLE: Button

    // ---- permissions (advertise/connect) ----
    private val permsForAdv: Array<String> by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            // برای تبلیغ روی <12 معمولاً پرمیشن زمان اجرا لازم نیست
            emptyArray()
        }
    }

    private val requestBlePerms =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            val granted = permsForAdv.all { p ->
                grants[p] == true || ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED
            }
            if (granted) {
                startBleInternal()
            } else {
                tvStatus.text = "اجازه BLE داده نشد"
            }
        }

    private fun ensureBlePermissions(onGranted: () -> Unit) {
        val need = permsForAdv.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (need.isEmpty()) onGranted() else requestBlePerms.launch(permsForAdv)
    }
    // -----------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvAmount = findViewById(R.id.tvAmount)
        tvStatus = findViewById(R.id.tvStatus)
        ivQR = findViewById(R.id.ivQR)
        btnGenerateQR = findViewById(R.id.btnGenerateQR)
        btnStartBLE = findViewById(R.id.btnStartBLE)
        btnStopBLE = findViewById(R.id.btnStopBLE)

        tvAmount.text = "200000"
        tvStatus.text = "آماده"

        btnGenerateQR.setOnClickListener { generateQR() }
        btnStartBLE.setOnClickListener { ensureBlePermissions { startBleInternal() } }
        btnStopBLE.setOnClickListener {
            bleService.stopAdvertising()
            tvStatus.text = "BLE متوقف شد"
        }
    }

    private fun startBleInternal() {
        // می‌تونی این payload رو هرچی لازم داری بسازی
        val payload = "SOMA|TX|${System.currentTimeMillis()}".toByteArray()

        if (!bleService.isReady(this)) {
            tvStatus.text = "این دستگاه BLE Peripheral را پشتیبانی نمی‌کند/اجازه ندارد"
            return
        }

        bleService.startAdvertising(
            context = this,
            payload = payload,
            onStart = { runOnUiThread { tvStatus.text = "فعال شد و در حال انتشار است BLE" } },
            onFail = { code -> runOnUiThread { tvStatus.text = "خطای BLE: $code" } }
        )
    }

    private fun generateQR() {
        val content = "SOMA|TX|${System.currentTimeMillis()}"
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 600, 600)
        val bmp = android.graphics.Bitmap.createBitmap(600, 600, android.graphics.Bitmap.Config.RGB_565)
        for (x in 0 until 600) {
            for (y in 0 until 600) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        ivQR.setImageBitmap(bmp)
        tvStatus.text = "QR ساخته شد"
    }

    override fun onDestroy() {
        super.onDestroy()
        bleService.stopAdvertising()
    }
}
