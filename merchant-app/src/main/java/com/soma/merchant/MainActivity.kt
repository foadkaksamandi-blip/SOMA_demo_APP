package com.soma.merchant

import android.Manifest
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.soma.merchant.ble.MerchantGattServer
import shared.utils.DateUtils // از فایل فعلی‌تان
import shared.utils.QRHandler   // از فایل فعلی‌تان
import org.json.JSONObject
import java.util.UUID

class MainActivity : AppCompatActivity() {

    // ویوها مطابق activity_main.xml فعلی
    private lateinit var editAmount: EditText
    private lateinit var btnGenerateQR: Button
    private lateinit var imageQR: ImageView
    private lateinit var txtStatus: TextView
    private lateinit var btnBleStart: Button
    private lateinit var btnBleStop: Button

    // UUIDها باید دقیقاً با اپ خریدار یکی باشند
    private val SERVICE_UUID: UUID = UUID.fromString("0000feed-0000-1000-8000-00805f9b34fb")
    private val CHAR_TX_UUID: UUID = UUID.fromString("0000beef-0000-1000-8000-00805f9b34fb")

    // گَت‌سرور BLE
    private var gattServer: MerchantGattServer? = null

    // همین استرینگ را هم در QR می‌گذاریم هم روی BLE می‌فرستیم
    private var lastOfferJson: String = ""

    // مجوزهای BLE در اندروید ۱۲+
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { /* نتیجه لازم نداریم */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editAmount   = findViewById(R.id.editAmount)
        btnGenerateQR = findViewById(R.id.btnGenerateQR)
        imageQR      = findViewById(R.id.imageQR)
        txtStatus    = findViewById(R.id.txtStatus)
        btnBleStart  = findViewById(R.id.btnBleStart)
        btnBleStop   = findViewById(R.id.btnBleStop)

        btnGenerateQR.setOnClickListener {
            val amount = (editAmount.text?.toString()?.trim()?.ifEmpty { "0" } ?: "0").toLong()
            lastOfferJson = buildOfferJson(amount)
            val bmp = makeQr(lastOfferJson)
            imageQR.setImageBitmap(bmp)
            txtStatus.text = "وضعیت: QR ساخته شد"
        }

        btnBleStart.setOnClickListener {
            ensureBlePermissions()
            if (lastOfferJson.isEmpty()) {
                // اگر هنوز QR/آفر ساخته نشده، الان بسازیم تا BLE همان را بدهد
                val amount = (editAmount.text?.toString()?.trim()?.ifEmpty { "0" } ?: "0").toLong()
                lastOfferJson = buildOfferJson(amount)
                imageQR.setImageBitmap(makeQr(lastOfferJson))
            }
            startBle()
        }

        btnBleStop.setOnClickListener {
            stopBle()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopBle()
    }

    /** ساخت JSON آفر خرید (سازگار با مرحله QR) */
    private fun buildOfferJson(amount: Long): String {
        val txId = DateUtils.generateTxId()      // از util خودتان
        val date = DateUtils.nowJalaliDate()
        val time = DateUtils.nowJalaliDateTime()
        val obj = JSONObject().apply {
            put("type", "purchase_offer")
            put("amount", amount)
            put("txId", txId)
            put("date", date)
            put("time", time)
            put("merchant", "SOMA-DEMO")
        }
        return obj.toString()
    }

    /** ساخت QR با همان JSON (از QRHandler خودتان استفاده می‌کنیم) */
    private fun makeQr(content: String): Bitmap {
        // اگر در QRHandler امضای متفاوتی دارید، فقط همین یک خط را با متد فعلی‌تان عوض کنید
        return QRHandler.create(content, 800)
    }

    private fun ensureBlePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(arrayOf(
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            ))
        }
        // اندرویدهای پایین‌تر فقط BLUETOOTH/LOCATION لازم دارند که در Manifest اضافه شده است
    }

    private fun startBle() {
        if (gattServer != null) return
        gattServer = MerchantGattServer(
            context = this,
            serviceUuid = SERVICE_UUID,
            charTxUuid = CHAR_TX_UUID
        ) { state ->
            runOnUiThread { txtStatus.text = "وضعیت: $state" }
        }
        gattServer?.start(lastOfferJson)
    }

    private fun stopBle() {
        gattServer?.stop()
        gattServer = null
        txtStatus.text = "وضعیت: BLE متوقف شد"
    }
}
