package com.soma.merchant

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.ParcelUuid
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.nio.charset.Charset
import java.util.UUID

/**
 * Foreground service for BLE Peripheral demo.
 * - Starts/stops LE advertising via Intent actions:
 *   * ACTION_START_ADVERTISING
 *   * ACTION_STOP_ADVERTISING
 * - Opens a simple GATT server with one service/characteristic
 * - Broadcasts human-readable status messages to MainActivity via LocalBroadcast
 */
class BlePeripheralService : Service() {

    private var advertiser: BluetoothLeAdvertiser? = null
    private var advertiseCallback: AdvertiseCallback? = null

    private var gattServer: BluetoothGattServer? = null

    // UUIDs (نمونه)
    private val SERVICE_UUID: UUID =
        UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB") // Battery Service as demo
    private val CHAR_UUID: UUID =
        UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB") // Battery Level

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startInForeground()
        setupGattServer()
    }

    override fun onDestroy() {
        stopAdvertisingInternal()
        closeGatt()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            MainActivity.ACTION_START_ADVERTISING -> startAdvertising()
            MainActivity.ACTION_STOP_ADVERTISING -> {
                stopAdvertisingInternal()
                sendStatus("انتشار BLE متوقف شد")
                stopSelf()
            }
            else -> { /* no-op */ }
        }
        return START_STICKY
    }

    // -------------------- Foreground --------------------

    private fun startInForeground() {
        val channelId = "ble_peripheral_demo"
        val channelName = "BLE Peripheral"
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (nm.getNotificationChannel(channelId) == null) {
                nm.createNotificationChannel(
                    NotificationChannel(
                        channelId,
                        channelName,
                        NotificationManager.IMPORTANCE_LOW
                    ).apply { setShowBadge(false) }
                )
            }
        }
        val notif: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("SOMA Merchant")
            .setContentText("سرویس BLE در حال اجرا")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setOngoing(true)
            .build()
        // برای API 29- باید foreground باشد تا تبلیغ مجاز شود
        startForeground(1001, notif)
    }

    // -------------------- GATT Server --------------------

    private fun setupGattServer() {
        val bm = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        gattServer = bm.openGattServer(this, object : BluetoothGattServerCallback() {
            override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
                super.onConnectionStateChange(device, status, newState)
                val s = when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> "دستگاه متصل شد"
                    BluetoothProfile.STATE_DISCONNECTED -> "دستگاه قطع شد"
                    else -> "تغییر وضعیت: $newState"
                }
                sendStatus(s)
            }

            override fun onCharacteristicReadRequest(
                device: BluetoothDevice?,
                requestId: Int,
                offset: Int,
                characteristic: BluetoothGattCharacteristic?
            ) {
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
                // مقدار نمایشی باتری (دمو)
                val value = byteArrayOf(85) // 85%
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, value)
            }
        })

        // add service + characteristic
        val service = BluetoothGattService(
            SERVICE_UUID,
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )
        val characteristic = BluetoothGattCharacteristic(
            CHAR_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        )
        service.addCharacteristic(characteristic)
        gattServer?.addService(service)
    }

    private fun closeGatt() {
        try {
            gattServer?.close()
        } catch (_: Exception) {
        }
        gattServer = null
    }

    // -------------------- Advertising --------------------

    private fun startAdvertising() {
        // مجوزها را دوباره چک می‌کنیم
        if (!hasAdvertisePermission()) {
            sendStatus("مجوزهای بلوتوث/مکان فراهم نیست")
            return
        }

        val bm = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bm.adapter
        if (adapter == null || !adapter.isEnabled) {
            sendStatus("بلوتوث فعال نیست")
            return
        }

        advertiser = adapter.bluetoothLeAdvertiser
        if (advertiser == null) {
            sendStatus("این دستگاه از BLE Peripheral پشتیبانی نمی‌کند")
            return
        }

        if (advertiseCallback != null) {
            sendStatus("در حال انتشار است…")
            return
        }

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build()

        // دادهٔ تبلیغ: نام و UUID سرویس
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()

        val scanResponse = AdvertiseData.Builder()
            .addServiceData(
                ParcelUuid(SERVICE_UUID),
                "SOMA".toByteArray(Charset.defaultCharset())
            )
            .build()

        advertiseCallback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                super.onStartSuccess(settingsInEffect)
                sendStatus("انتشار BLE شروع شد")
            }

            override fun onStartFailure(errorCode: Int) {
                super.onStartFailure(errorCode)
                advertiseCallback = null
                val msg = when (errorCode) {
                    ADVERTISE_FAILED_ALREADY_STARTED -> "قبلاً شروع شده بود"
                    ADVERTISE_FAILED_DATA_TOO_LARGE -> "دادهٔ تبلیغ بزرگ است"
                    ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> "ویژگی پشتیبانی نمی‌شود"
                    ADVERTISE_FAILED_INTERNAL_ERROR -> "خطای داخلی"
                    ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> "تبلیغ‌کننده زیاد است"
                    else -> "خطای ناشناخته: $errorCode"
                }
                sendStatus("خطا در شروع انتشار: $msg")
            }
        }

        advertiser?.startAdvertising(settings, data, scanResponse, advertiseCallback)
        sendStatus("در حال تلاش برای انتشار…")
    }

    private fun stopAdvertisingInternal() {
        try {
            advertiser?.stopAdvertising(advertiseCallback)
        } catch (_: Exception) {
        }
        advertiseCallback = null
    }

    // -------------------- Utils --------------------

    private fun hasAdvertisePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= 31) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_ADVERTISE
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // در نسخه‌های قدیمی‌تر معمولاً FINE_LOCATION لازم است
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun sendStatus(text: String) {
        val i = Intent(MainActivity.ACTION_BLE_STATUS)
            .putExtra(MainActivity.EXTRA_STATUS_TEXT, text)
        LocalBroadcastManager.getInstance(this).sendBroadcast(i)
    }
}
