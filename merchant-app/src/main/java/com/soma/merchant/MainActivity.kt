package com.soma.merchant

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.soma.merchant.ble.BLEPeripheralService
import org.json.JSONObject
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var edtAmount: EditText
    private lateinit var ivQr: ImageView
    private lateinit var btnGenQr: Button
    private lateinit var btnStartBle: Button
    private lateinit var btnStopBle: Button

    private lateinit var blePeripheralService: BLEPeripheralService

    private val blePerms =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) arrayOf(
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        ) else emptyArray()

    private val reqPermsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val allGranted = result.values.all { it }
            if (allGranted) startBleIfReady()
            else toast("اجازه‌های بلوتوث داده نشد")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        edtAmount = findViewById(R.id.edtAmount)
        ivQr = findViewById(R.id.ivQr)
        btnGenQr = findViewById(R.id.btnGenQr)
        btnStartBle = findViewById(R.id.btnStartBle)
        btnStopBle = findViewById(R.id.btnStopBle)

        blePeripheralService = BLEPeripheralService(this)

        // دکمه QR: ساخت QR واقعی از مبلغ
        btnGenQr.setOnClickListener { generateQr() }

        // دکمه‌های BLE
        btnStartBle.setOnClickListener { ensurePermsThenStart() }
        btnStopBle.setOnClickListener {
            blePeripheralService.stopAdvertising()
            tvStatus.text = "وضعیت: توقف BLE"
        }

        // شروع خودکار BLE (اختیاری)
        ensurePermsThenStart()
    }

    private fun ensurePermsThenStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val need = blePerms.any {
                ContextCompat.checkSelfPermission(this, it) != PermissionChecker.PERMISSION_GRANTED
            }
            if (need) {
                reqPermsLauncher.launch(blePerms)
                return
            }
        }
        startBleIfReady()
    }

    private fun startBleIfReady() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            tvStatus.text = "این دستگاه بلوتوث ندارد"
            return
        }
        if (!adapter.isEnabled) {
            startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
            toast("لطفاً بلوتوث را روشن کنید")
            return
        }
        try {
            blePeripheralService.startAdvertising()
            tvStatus.text = "وضعیت: BLE Advertising فعال شد"
        } catch (t: Throwable) {
            tvStatus.text = "خطا در شروع BLE: ${t.message}"
        }
    }

    private fun generateQr() {
        val amountText = edtAmount.text?.toString()?.trim().orEmpty()
        val amount = amountText.toLongOrNull()
        if (amount == null || amount <= 0) {
            toast("مبلغ معتبر وارد کنید")
            return
        }

        // ساخت payload واقعی برای اسکن توسط اپ خریدار
        val payload = JSONObject().apply {
            put("type", "PAY_REQUEST")
            put("merchantId", "M-001")
            put("amount", amount)
            put("txId", UUID.randomUUID().toString())
            put("ts", System.currentTimeMillis())
        }.toString()

        try {
            val hints = hashMapOf(EncodeHintType.CHARACTER_SET to "UTF-8")
            val bitMatrix = MultiFormatWriter().encode(payload, BarcodeFormat.QR_CODE, 900, 900, hints)
            val bitmap = BarcodeEncoder().createBitmap(bitMatrix)
            ivQr.setImageBitmap(bitmap)
            tvStatus.text = "QR تولید شد (txId داخل payload)"
        } catch (t: Throwable) {
            tvStatus.text = "خطا در تولید QR: ${t.message}"
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    override fun onDestroy() {
        super.onDestroy()
        blePeripheralService.stopAdvertising()
    }
}
