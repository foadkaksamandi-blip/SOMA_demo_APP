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

        bleClient = BleClient(this)

        binding.btnStartBle.setOnClickListener {
            bleClient?.startScan()
        }

        binding.btnStopBle.setOnClickListener {
            bleClient?.stopScan()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bleClient?.stopScan()
    }
}
