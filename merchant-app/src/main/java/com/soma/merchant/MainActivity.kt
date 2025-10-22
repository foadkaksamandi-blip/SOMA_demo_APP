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

    // سرویس BLE (بعد از اجازه ساخته می‌شود)
    private var ble: BLEPeripheralService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        edtAmount = findViewById(R.id.edtAmount)
        btnGenQr = findViewById(R.id.btnGenQr)
        imgQr = findViewById(R.id.ivQr)
        btnStartBle = findViewById(R.id.btnStartBle)
        btnStopBle = findViewById(R.id.btnStopBle)

        // اجازه‌ها سپس init
        checkAndRequestPerms { initAfterPerms() }

        btnGenQr.setOnClickListener {
            val amount = edtAmount.text.toString()
            if (amount.isEmpty()) {
                Toast.makeText(this, "مبلغ را وارد کنید", Toast.LENGTH_SHORT).show()
            } else {
                // این‌جا QR تولید کن (فعلاً فقط پیام):
                tvStatus.text = "QR ایجاد شد برای مبلغ $amount"
                // اگر کلاس تولید QR داری این‌جا تصویر را در imgQr بگذار.
            }
        }

        btnStartBle.setOnClickListener {
            if (ensureBleReady()) {
                ble?.startAdvertising(this)
                tvStatus.text = "BLE فعال شد ✅"
            }
        }

        btnStopBle.setOnClickListener {
            ble?.stopAdvertising()
            tvStatus.text = "BLE متوقف شد 🛑"
        }
    }

    private fun initAfterPerms() {
        try {
            ble = BLEPeripheralService()
        } catch (t: Throwable) {
            Toast.makeText(this, "Init error: ${t.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkAndRequestPerms(onGranted: () -> Unit) {
        val needs = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= 31) {
            needs += listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
        } else {
            needs += Manifest.permission.ACCESS_FINE_LOCATION
        }
        needs += Manifest.permission.CAMERA

        val toAsk = needs.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (toAsk.isEmpty()) onGranted()
        else ActivityCompat.requestPermissions(this, toAsk.toTypedArray(), 201)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 201 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            initAfterPerms()
        } else {
            Toast.makeText(this, "اجازه‌ها لازم‌اند", Toast.LENGTH_LONG).show()
        }
    }

    private fun ensureBleReady(): Boolean {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            Toast.makeText(this, "بلوتوث در دسترس نیست", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        ble?.stopAdvertising()
    }
}
