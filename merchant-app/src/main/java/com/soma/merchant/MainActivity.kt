package com.soma.merchant

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.soma.merchant.ble.BLEPeripheralService

class MainActivity : AppCompatActivity() {

    private lateinit var blePeripheralService: BLEPeripheralService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        blePeripheralService = BLEPeripheralService(this)

        // شروع تبلیغ BLE هنگام اجرا
        blePeripheralService.startAdvertising()
    }

    override fun onDestroy() {
        super.onDestroy()
        blePeripheralService.stopAdvertising()
    }
}
