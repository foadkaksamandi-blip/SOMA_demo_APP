package com.soma.consumer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.soma.consumer.databinding.ActivityMainBinding
import com.soma.consumer.qr.QrScanner

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var qrScanner: QrScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvStatus.text = "وضعیت: آماده"

        qrScanner = QrScanner(this) { contents ->
            if (contents.isNullOrEmpty()) {
                binding.tvResult.text = "نتیجه: لغو شد/ناموفق"
                binding.tvStatus.text = "وضعیت: اسکن ناموفق"
            } else {
                binding.tvResult.text = "نتیجه: $contents"
                binding.tvStatus.text = "وضعیت: اسکن موفق"
            }
        }

        binding.btnScanQr.setOnClickListener { qrScanner.start(this) }

        // دکمه‌های BLE فعلاً placeholder تا کد BLE وصل شود
        binding.btnStartBle.setOnClickListener {
            binding.tvStatus.text = "وضعیت: BLE شروع شد (دمو)"
        }
        binding.btnStopBle.setOnClickListener {
            binding.tvStatus.text = "وضعیت: BLE متوقف شد (دمو)"
        }
    }
}
