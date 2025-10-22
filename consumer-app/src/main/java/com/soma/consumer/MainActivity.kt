package com.soma.consumer

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.soma.consumer.ble.BleClient
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var bleClient: BleClient
    private lateinit var logView: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->
            // بعد از درخواست مجوزها
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logView = findViewById(R.id.logView)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)

        bleClient = BleClient(
            context = this,
            onLog = { appendLog(it) },
            onTxnResult = { success, msg ->
                appendLog("Result: $success, $msg")
            }
        )

        btnStart.setOnClickListener {
            checkPermissionsAndStart()
        }

        btnStop.setOnClickListener {
            bleClient.stop()
            appendLog("Stopped manually")
        }
    }

    private fun appendLog(text: String) {
        runOnUiThread {
            logView.append(text + "\n")
        }
    }

    private fun checkPermissionsAndStart() {
        val required = mutableListOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
        )
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            required.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val missing = required.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
            appendLog("Requesting permissions…")
        } else {
            startBleScan()
        }
    }

    private fun startBleScan() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null || !adapter.isEnabled) {
            appendLog("Bluetooth OFF")
            return
        }
        appendLog("Starting BLE scan…")
        bleClient.start(UUID.fromString("0000feed-0000-1000-8000-00805f9b34fb"))
    }
}
