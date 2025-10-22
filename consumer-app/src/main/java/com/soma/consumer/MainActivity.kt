package com.soma.consumer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.soma.consumer.databinding.ActivityMainBinding
import com.soma.consumer.ble.BleClient

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var bleClient: BleClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // BleClient با callback تعریف می‌شود
        bleClient = BleClient(
            context = this,
            onFound = { device ->
                // وقتی دستگاه پیدا شد، روی صفحه نشان داده شود (اختیاری)
                binding.txtStatus.text = "Found: ${device.name ?: "Unknown"}"
            }
        )

        binding.btnStartBle.setOnClickListener {
            bleClient?.startScan()
            binding.txtStatus.text = "Scanning..."
        }

        binding.btnStopBle.setOnClickListener {
            bleClient?.stopScan()
            binding.txtStatus.text = "Stopped"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bleClient?.stopScan()
    }
}
