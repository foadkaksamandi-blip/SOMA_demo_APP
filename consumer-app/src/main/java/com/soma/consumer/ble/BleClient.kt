package com.soma.consumer.ble

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.soma.consumer.shared.Protocol
import java.nio.charset.StandardCharsets

class BleClient(private val context: Context) {
    private val btManager = context.getSystemService(BluetoothManager::class.java)
    private val adapter = btManager.adapter
    private var gatt: BluetoothGatt? = null
    private var onResult: ((String)->Unit)? = null

    fun start(amount: String, txId: String, onLog: (String)->Unit, onTxnResult: (Boolean, String)->Unit) {
        onResult = { msg ->
            val ok = msg.startsWith("OK|")
            val rTx = msg.substringAfter("|")
            onTxnResult(ok, rTx)
        }
        val scanner = adapter.bluetoothLeScanner
        val filter = ScanFilter.Builder().setServiceUuid(ParcelUuid(Protocol.SERVICE_UUID)).build()
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        scanner.startScan(listOf(filter), settings, object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                scanner.stopScan(this)
                onLog("دستگاه یافت شد: ${result.device.name ?: result.device.address}")
                gatt = result.device.connectGatt(context, false, gattCallback)
                // پیام پرداخت را بعد از اتصال می‌فرستیم
                pendingCmd = "$amount|$txId"
            }
            override fun onScanFailed(errorCode: Int) {
                onLog("اسکن شکست خورد: $errorCode")
            }
        })
        onLog("در حال اسکن سرویس Merchant…")
    }

    private var pendingCmd: String? = null

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices()
            }
        }
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            val service = gatt.getService(Protocol.SERVICE_UUID) ?: return
            val res = service.getCharacteristic(Protocol.CHAR_RESULT_UUID)
            gatt.setCharacteristicNotification(res, true)
            // ارسال فرمان پرداخت
            val cmd = service.getCharacteristic(Protocol.CHAR_CMD_UUID)
            val msg = (pendingCmd ?: "0|UNKNOWN").toByteArray(StandardCharsets.UTF_8)
            cmd.value = msg
            gatt.writeCharacteristic(cmd)
        }
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            if (characteristic.uuid == Protocol.CHAR_RESULT_UUID) {
                val msg = String(characteristic.value, StandardCharsets.UTF_8)
                onResult?.invoke(msg)
                gatt.disconnect()
                gatt.close()
            }
        }
    }
}
