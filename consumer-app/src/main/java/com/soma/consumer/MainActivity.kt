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

    private val enableBt =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bleClient = BleClient(
            context = this,
            statusCallback = { text -> binding.tvStatus.text = text }
        )

        qrScanner = QRScanner(
            activity = this,
            onResult = { text -> binding.tvResult.text = text }
        )

        binding.btnQr.setOnClickListener { qrScanner.startScan() }
        binding.btnStart.setOnClickListener { bleClient.startScanAndConnect(enableBt) }
        binding.btnStop.setOnClickListener { bleClient.stop() }
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
