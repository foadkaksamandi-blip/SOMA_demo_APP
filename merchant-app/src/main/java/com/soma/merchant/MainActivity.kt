package com.soma.merchant

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.soma.merchant.ble.BLEPeripheralService

class MainActivity : AppCompatActivity() {

    // View ها
    private lateinit var tvStatus: TextView
    private lateinit var edtAmount: EditText
    private lateinit var btnGenQr: Button
    private lateinit var imgQr: ImageView
    private lateinit var btnStartBle: Button
    private lateinit var btnStopBle: Button

    // سرویس Peripheral
    private var ble: BLEPeripheralService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind
        tvStatus = findViewById(R.id.tvStatus)
        edtAmount = findViewById(R.id.edtAmount)
        btnGenQr = findViewById(R.id.btnGenQr)
        imgQr = findViewById(R.id.ivQr)
        btnStartBle = findViewById(R.id.btnStartBle)
        btnStopBle = findViewById(R.id.btnStopBle)

        // Init BLE Peripheral safely
        try {
            ble = BLEPeripheralService()
        } catch (e: Throwable) {
            ble = null
            tvStatus.text = "خطا در راه‌اندازی BLE"
        }

        btnGenQr.setOnClickListener {
            val amount = edtAmount.text?.toString()?.trim().orEmpty()
            if (amount.isEmpty()) {
                Toast.makeText(this, "مبلغ را وارد کنید", Toast.LENGTH_SHORT).show()
            } else {
                // اینجا QR را با روش خودت بساز (اگر کلاس/متد آماده داری)
                tvStatus.text = "QR آماده شد برای مبلغ $amount"
                // imgQr.setImageBitmap(qrBitmap)  // در صورت داشتن خروجی
            }
        }

        btnStartBle.setOnClickListener {
            if (!ensureBleAdvertisePermissions()) return@setOnClickListener
            ble?.startAdvertising(this)
            tvStatus.text = "BLE فعال شد ✅"
        }

        btnStopBle.setOnClickListener {
            ble?.stopAdvertising()
            tvStatus.text = "BLE متوقف شد 🛑"
        }
    }

    private fun ensureBleAdvertisePermissions(): Boolean {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            Toast.makeText(this, "بلوتوث در دستگاه وجود ندارد", Toast.LENGTH_SHORT).show()
            return false
        }

        val needed = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE)
                != PackageManager.PERMISSION_GRANTED
            ) needed += Manifest.permission.BLUETOOTH_ADVERTISE

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED
            ) needed += Manifest.permission.BLUETOOTH_CONNECT
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) needed += Manifest.permission.ACCESS_FINE_LOCATION
        }

        return if (needed.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toTypedArray(), 101)
            false
        } else true
    }

    override fun onDestroy() {
        super.onDestroy()
        try { ble?.stopAdvertising() } catch (_: Throwable) {}
    }
}
