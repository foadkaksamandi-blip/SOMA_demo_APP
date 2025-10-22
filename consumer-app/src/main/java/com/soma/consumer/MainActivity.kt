package com.soma.consumer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.soma.consumer.ble.BleClient
import android.util.Log
import com.soma.consumer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bleClient: BleClient
    private val TAG = "ConsumerMain"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bleClient = BleClient(this)

        binding.btnStartBle.setOnClickListener {
            // example: start scanning
            bleClient.startScan { address ->
                Log.d(TAG, "Found: $address")
                // update UI
            }
        }

        binding.btnStopBle.setOnClickListener {
            bleClient.disconnect()
        }
    }
}
