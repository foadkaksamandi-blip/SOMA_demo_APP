package com.soma.merchant

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.soma.merchant.ble.MerchantGattServer
import org.json.JSONObject
import shared.utils.DateUtils
import shared.utils.QRHandler
import java.util.UUID

class MainActivity : AppCompatActivity() {

    // Views (مطابق layout موجود)
    private lateinit var editAmount: EditText
    private lateinit var btnGenerateQR: Button
    private lateinit var imageQR: ImageView
    private lateinit var txtStatus: TextView
    private lateinit var btnBleStart: Button
    private lateinit var btnBleStop: Button

    // UUIDها باید با اپ خریدار (BleClient) دقیقاً یکسان باشند
    private val SERVICE_UUID: UUID = UUID.fromString("0000feed-0000-1000-8000-00805f9b34fb")
    private val CHAR_TX_UUID: UUID = UUID.fromString("0000beef-0000-1000-8000-00805f9b34fb")

    // BLE GATT Server
    private var gattServer: MerchantGattServer? = null

    // پیام آفر خرید که هم در QR نمایش داده می‌شود و هم از طریق BLE ارسال می‌گردد
    private var lastOfferJson: String = ""

    // درخواست مجوزهای BLE برای Android 12+
    private val blePermsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        wireUi()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopBle()
    }

    private fun bindViews() {
        editAmount   = findViewById(R.id.editAmount)
        btnGenerateQR = findViewById(R.id.btnGenerateQR)
        imageQR      = findViewById(R.id.imageQR)
        txtStatus    = findViewById(R.id.txtStatus)
        btnBleStart  = findViewById(R.id.btnBleStart)
        btnBleStop   = findViewById(R.id.btnBleStop)
    }

    private fun wireUi() {
        btnGenerateQR.setOnClickListener {
            val amount = (editAmount.text?.toString()?.trim()?.ifEmpty { "0" } ?: "0").toLong()
            lastOfferJson = buildOfferJson(amount)
            imageQR.setImageBitmap(QRHandler.create(lastOfferJson, 800))
            txtStatus.text = "وضعیت: QR ساخته شد"
        }

        btnBleStart.setOnClickListener {
            ensureBlePermissions()
            // اگر هنوز آفر ساخته نشده، همین‌جا بساز
            if (lastOfferJson.isEmpty()) {
                val amount = (editAmount.text?.toString()?.trim()?.ifEmpty { "0" } ?: "0").toLong()
                lastOfferJson = buildOfferJson(amount)
                imageQR.setImageBitmap(QRHandler.create(lastOfferJson, 800))
            }
            startBle()
        }

        btnBleStop.setOnClickListener { stopBle() }
    }

    /** ساخت JSON آفر خرید هماهنگ با اسکن خریدار */
    private fun buildOfferJson(amount: Long): String {
        val txId = DateUtils.generateTxId()
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

    private fun ensureBlePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val perms = arrayOf(
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
            blePermsLauncher.launch(perms)
        }
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
