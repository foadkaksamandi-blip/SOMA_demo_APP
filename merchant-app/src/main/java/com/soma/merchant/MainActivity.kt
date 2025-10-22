package com.soma.merchant

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.soma.merchant.ble.BLEPeripheralService

class MainActivity : AppCompatActivity() {

    private lateinit var btnStartBle: Button
    private lateinit var btnStopBle: Button
    private lateinit var tvStatus: TextView

    private val ble = BLEPeripheralService()

    private val enableBtLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* ignore */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnStartBle = findViewById(R.id.btnStartBle)
        btnStopBle = findViewById(R.id.btnStopBle)
        tvStatus = findViewById(R.id.tvStatus)

        btnStartBle.setOnClickListener {
            if (ensureBluetoothReady()) {
                ble.startAdvertising(this)
                tvStatus.text = "وضعیت: ادورتایز فعال"
            }
        }

        btnStopBle.setOnClickListener {
            ble.stopAdvertising()
            tvStatus.text = "وضعیت: متوقف شد"
        }
    }

    private fun ensureBluetoothReady(): Boolean {
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return false

        if (!adapter.isEnabled) {
            enableBtLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            return false
        }

        // دسترسی‌ها
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
