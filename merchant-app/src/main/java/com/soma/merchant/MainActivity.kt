package com.soma.merchant

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.soma.merchant.ble.BLEPeripheralService

class MainActivity : AppCompatActivity() {

    private lateinit var ble: BLEPeripheralService

    // UI
    private lateinit var tvStatus: TextView
    private lateinit var edAmount: EditText
    private lateinit var btnGenQr: Button
    private lateinit var imgQr: ImageView
    private lateinit var btnStartBle: Button
    private lateinit var btnStopBle: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind views
        tvStatus = findViewById(R.id.tvStatus)
        edAmount = findViewById(R.id.edtAmount)
        btnGenQr = findViewById(R.id.btnGenQr)
        imgQr = findViewById(R.id.ivQr)
        btnStartBle = findViewById(R.id.btnStartBle)
        btnStopBle = findViewById(R.id.btnStopBle)

        ble = BLEPeripheralService()

        // ساخت QR (اگر از تصویر استفاده می‌کنی؛ در غیر این صورت می‌توانی حذف کنی)
        btnGenQr.setOnClickListener {
            val amount = edAmount.text.toString()
            if (amount.isEmpty()) {
                Toast.makeText(this, "لطفاً مبلغ را وارد کنید", Toast.LENGTH_SHORT).show()
            } else {
                // اینجا اگر ژنراتور QR داری، تصویر را بگذار داخل imgQr و وضعیت بده
                tvStatus.text = "QR آماده ارسال ✅"
            }
        }

        // شروع تبلیغ BLE
        btnStartBle.setOnClickListener {
            if (ensureBluetoothReady()) {
                ble.startAdvertising(this)
                tvStatus.text = "BLE فعال شد ✅"
            }
        }

        // توقف تبلیغ BLE
        btnStopBle.setOnClickListener {
            ble.stopAdvertising()
            tvStatus.text = "BLE متوقف شد ⛔"
        }
    }

    // ----- Permissions -----

    private fun ensureBluetoothReady(): Boolean {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            Toast.makeText(this, "این دستگاه بلوتوث ندارد", Toast.LENGTH_SHORT).show()
            return false
        }

        val needed = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.BLUETOOTH_ADVERTISE
                ) != PackageManager.PERMISSION_GRANTED
            ) needed += Manifest.permission.BLUETOOTH_ADVERTISE

            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) needed += Manifest.permission.BLUETOOTH_CONNECT
        } else {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) needed += Manifest.permission.ACCESS_FINE_LOCATION
        }

        return if (needed.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toTypedArray(), 100)
            false
        } else {
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ble.stopAdvertising()
    }
}
