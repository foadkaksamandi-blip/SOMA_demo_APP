package com.soma.consumer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.soma.consumer.ble.BleClient
import com.soma.consumer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bleClient: BleClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bleClient = BleClient(this)

        binding.btnScanQr.setOnClickListener {
            binding.tvResult.text = "نتیجه: اسکن QR (نمونه)"
        }

        binding.btnStart.setOnClickListener {
            ensureBlePermissions {
                binding.tvStatus.text = "وضعیت: تلاش برای اتصال BLE"
                bleClient.start(
                    onConnected = { runOnUiThread { binding.tvStatus.text = "وضعیت: متصل" } },
                    onDisconnected = { runOnUiThread { binding.tvStatus.text = "وضعیت: قطع اتصال" } },
                    onError = { msg -> runOnUiThread { binding.tvStatus.text = "خطا: $msg" } }
                )
            }
        }

        binding.btnStop.setOnClickListener {
            bleClient.stop()
            binding.tvStatus.text = "وضعیت: متوقف شد"
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    private fun ensureBlePermissions(onGranted: () -> Unit) {
        val perms = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms += listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        }
        val need = perms.any {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (need) permissionLauncher.launch(perms.toTypedArray()) else onGranted()
    }
}
