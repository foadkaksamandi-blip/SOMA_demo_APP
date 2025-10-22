package com.soma.merchant

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.soma.merchant.databinding.ActivityMainBinding
import com.soma.merchant.ble.BlePeripheralService

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var bleService: BlePeripheralService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bleService = BlePeripheralService(this)

        binding.btnStartBle.setOnClickListener {
            bleService?.startAdvertising()
        }

        binding.btnStopBle.setOnClickListener {
            bleService?.stopAdvertising()
        }

        binding.btnPay.setOnClickListener {
            bleService?.simulatePayment()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bleService?.stopAdvertising()
    }
}
