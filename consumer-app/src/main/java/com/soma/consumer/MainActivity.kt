package com.soma.consumer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.soma.consumer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // اگر کلاس‌های BLE و QR شما نام‌های دیگری دارند، این‌ها را مطابق پروژه خود تغییر بدهید
    private val ble by lazy { com.soma.consumer.ble.BleClient(this) }
    private val qrScanner by lazy { com.soma.consumer.qr.QrScanner(this) }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* در این دمو فقط درخواست می‌کنیم؛ هندل لازم نیست */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestNeededPermissions()

        binding.btnQr.setOnClickListener {
            qrScanner.startScan(
                onResult = { text ->
                    binding.tvResult.text = "نتیجه: $text"
                },
                onError = { e ->
                    binding.tvResult.text = "خطا در QR: ${e.message}"
                }
            )
        }

        binding.btnStart.setOnClickListener {
            binding.tvStatus.text = "وضعیت: تلاش برای اتصال BLE"
            ble.start(
                onConnected = { binding.tvStatus.text = "وضعیت: متصل" },
                onDisconnected = { binding.tvStatus.text = "وضعیت: قطع اتصال" },
                onError = { e -> binding.tvStatus.text = "خطای BLE: ${e.message}" }
            )
        }

        binding.btnStop.setOnClickListener {
            ble.stop()
            binding.tvStatus.text = "وضعیت: متوقف"
        }
    }

    private fun requestNeededPermissions() {
        val needs = mutableListOf<String>()
        // دوربین برای QR
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            needs += Manifest.permission.CAMERA
        }
        // BLE (Android 12+)
        val blePerms = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
        blePerms.forEach {
            if (ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED) {
                needs += it
            }
        }
        if (needs.isNotEmpty()) permissionLauncher.launch(needs.toTypedArray())
    }

    override fun onDestroy() {
        super.onDestroy()
        ble.stop()
    }
}
