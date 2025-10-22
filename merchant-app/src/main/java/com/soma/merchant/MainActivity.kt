package com.soma.merchant

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.soma.merchant.ble.BLEPeripheralService

class MainActivity : AppCompatActivity() {

    private val ble = BLEPeripheralService()

    private val enableBtLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // تلاش برای دریافت ویوها (ممکن است در لایه فعلی نام‌ها فرق داشته باشد)
        val btnStart: Button? = findViewById(R.id.btnStartBle)
        val btnStop: Button?  = findViewById(R.id.btnStopBle)
        val tvStatus: TextView? = findViewById(R.id.tvStatus)

        // اگر هرکدام نبود، پیغام بده ولی کرش نکن
        if (btnStart == null) Toast.makeText(this, "btnStartBle در layout پیدا نشد", Toast.LENGTH_SHORT).show()
        if (btnStop  == null) Toast.makeText(this, "btnStopBle در layout پیدا نشد", Toast.LENGTH_SHORT).show()
        if (tvStatus == null) Toast.makeText(this, "tvStatus در layout پیدا نشد", Toast.LENGTH_SHORT).show()

        btnStart?.setOnClickListener {
            if (ensureBluetoothReady()) {
                ble.startAdvertising(this)
                tvStatus?.text = "وضعیت: ادورتایز فعال"
            }
        }

        btnStop?.setOnClickListener {
            ble.stopAdvertising()
            tvStatus?.text = "وضعیت: متوقف شد"
        }
    }

    private fun ensureBluetoothReady(): Boolean {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            Toast.makeText(this, "این دستگاه بلوتوث ندارد", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!adapter.isEnabled) {
            enableBtLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
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
            ActivityCompat.requestPermissions(this, needed.toTypedArray(), 10)
            false
        } else true
    }

    override fun onDestroy() {
        super.onDestroy()
        ble.stopAdvertising()
    }
}
