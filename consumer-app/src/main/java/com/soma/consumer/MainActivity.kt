package com.soma.consumer

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.soma.consumer.databinding.ActivityMainBinding
import com.soma.consumer.ble.BleClient
import com.soma.consumer.qr.QRScanner

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bleClient: BleClient
    private lateinit var qrScanner: QRScanner

    // اگر برای فعال‌سازی بلوتوث یا مجوزها نیاز شد:
    private val enableBt =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { /* result ignored */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ مقداردهی صحیح ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // BLE
        bleClient = BleClient(this) { stateText ->
            // آپدیت متن وضعیت — دقت کن .text استفاده شده
            binding.tvStatus.text = stateText
        }

        // QR
        qrScanner = QRScanner(this) { qrText ->
            binding.tvResult.text = qrText
        }

        // دکمه‌ها
        binding.btnQr.setOnClickListener {
            qrScanner.startScan()
        }
        binding.btnStart.setOnClickListener {
            bleClient.startScanAndConnect(enableBt)
        }
        binding.btnStop.setOnClickListener {
            bleClient.stop()
        }
    }

    override fun onResume() {
        super.onResume()
        bleClient.onResume()
    }

    override fun onPause() {
        super.onPause()
        bleClient.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        bleClient.close()
    }
}
