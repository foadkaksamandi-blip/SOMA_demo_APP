package com.soma.merchant

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.soma.merchant.ble.BLEPeripheralService

class MainActivity : AppCompatActivity() {

    private lateinit var ble: BLEPeripheralService
    private lateinit var tvStatus: TextView
    private lateinit var edtAmount: EditText
    private lateinit var btnGenQr: Button
    private lateinit var imgQr: ImageView
    private lateinit var btnStartBle: Button
    private lateinit var btnStopBle: Button

    private val enableBtLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* ignored */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind views
        tvStatus = findViewById(R.id.tvStatus)
        edtAmount = findViewById(R.id.edtAmount)
        btnGenQr = findViewById(R.id.btnGenQr)
        imgQr = findViewById(R.id.ivQr)
        btnStartBle = findViewById(R.id.btnStartBle)
        btnStopBle = findViewById(R.id.btnStopBle)

        ble = BLEPeripheralService()

        btnGenQr.setOnClickListener {
            val amount = edtAmount.text.toString()
            if (amount.isEmpty()) {
                Toast.makeText(this, "مبلغ را وارد کنید", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "کد QR برای مبلغ $amount تولید شد", Toast.LENGTH_SHORT).show()
                tvStatus.text = "QR آماده ارسال"
                // در آینده QR واقعی اضافه می‌کنیم
            }
        }

        btnStartBle.setOnClickListener {
            if (ensureBluetoothReady()) {
                ble.startAdvertising(this)
                tvStatus.text = "BLE فعال شد ✅"
            }
        }

        btnStopBle.setOnClickListener {
            ble.stopAdvertising()
            tvStatus.text = "BLE متوقف شد ⛔"
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
            ActivityCompat.requestPermissions(this, needed.toTypedArray(), 100)
            false
        } else true
    }

    override fun onDestroy() {
        super.onDestroy()
        ble.stopAdvertising()
    }
}
