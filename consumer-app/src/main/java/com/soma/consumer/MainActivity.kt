package com.soma.consumer

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.zxing.integration.android.IntentIntegrator

class MainActivity : AppCompatActivity() {

    private lateinit var btnScanQR: Button
    private lateinit var btnBleStart: Button
    private lateinit var btnBleStop: Button
    private lateinit var statusTv: TextView

    private val permsBle12p = arrayOf(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN
    )

    private val permsLegacy = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private val requestPerms =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            // فقط جهت دمو
            if (result.values.any { it }) {
                Toast.makeText(this, "مجوزها داده شد", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "مجوز رد شد", Toast.LENGTH_SHORT).show()
            }
        }

    private val requestCamera =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startQrScan()
            else Toast.makeText(this, "مجوز دوربین لازم است", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnScanQR = findViewById(R.id.btnScanQR)
        btnBleStart = findViewById(R.id.btnBleStart)
        btnBleStop = findViewById(R.id.btnBleStop)
        statusTv = findViewById(R.id.txtStatus)

        btnScanQR.setOnClickListener { ensureCameraThenScan() }
        btnBleStart.setOnClickListener { startBleDemo() }
        btnBleStop.setOnClickListener { stopBleDemo() }
    }

    private fun ensureCameraThenScan() {
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) startQrScan() else requestCamera.launch(Manifest.permission.CAMERA)
    }

    private fun startQrScan() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("اسکن QR")
        integrator.setBeepEnabled(false)
        integrator.setOrientationLocked(false)
        integrator.initiateScan()
    }

    @Deprecated("onActivityResult is used by IntentIntegrator for simplicity")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                statusTv.text = "نتیجه اسکن: ${result.contents}"
                Toast.makeText(this, "دریافت شد", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "لغو شد", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun startBleDemo() {
        ensureBlePerms()
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            Toast.makeText(this, "BLE پشتیبانی نمی‌شود", Toast.LENGTH_SHORT).show()
            return
        }
        if (!adapter.isEnabled) {
            Toast.makeText(this, "بلوتوث خاموش است", Toast.LENGTH_SHORT).show()
        } else {
            // اینجا بعداً اسکن واقعی BLE را اضافه می‌کنیم
            Toast.makeText(this, "BLE (خریدار) شروع شد (دمو)", Toast.LENGTH_SHORT).show()
            statusTv.text = "وضعیت: BLE شروع شد"
        }
    }

    private fun stopBleDemo() {
        // اینجا بعداً توقف اسکن واقعی را اضافه می‌کنیم
        Toast.makeText(this, "BLE متوقف شد (دمو)", Toast.LENGTH_SHORT).show()
        statusTv.text = "وضعیت: BLE متوقف شد"
    }

    private fun ensureBlePerms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!permsBle12p.all {
                    ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
                }) {
                requestPerms.launch(permsBle12p)
            }
        } else {
            if (!permsLegacy.all {
                    ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
                }) {
                requestPerms.launch(permsLegacy)
            }
        }
    }
}
