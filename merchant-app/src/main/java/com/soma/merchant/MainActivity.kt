package com.soma.merchant

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.util.EnumMap

class MainActivity : AppCompatActivity() {

    private lateinit var amountEt: EditText
    private lateinit var btnGenerateQR: Button
    private lateinit var btnBleStart: Button
    private lateinit var btnBleStop: Button
    private lateinit var statusTv: TextView

    private val BLE_PERMS_12P = arrayOf(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_ADVERTISE
    )
    private val BLE_PERMS_LEGACY = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    private val REQ_BLE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        amountEt = findViewById(R.id.editAmount)
        btnGenerateQR = findViewById(R.id.btnGenerateQR)
        btnBleStart = findViewById(R.id.btnBleStart)
        btnBleStop = findViewById(R.id.btnBleStop)
        statusTv = findViewById(R.id.txtStatus)

        btnGenerateQR.setOnClickListener { onGenerateQR() }
        btnBleStart.setOnClickListener { startBleDemo() }
        btnBleStop.setOnClickListener { stopBleDemo() }
    }

    private fun onGenerateQR() {
        val amount = amountEt.text.toString().trim()
        if (amount.isEmpty()) {
            Toast.makeText(this, "مبلغ را وارد کنید", Toast.LENGTH_SHORT).show()
            return
        }
        val payload = "SOMA|MERCHANT|AMOUNT=$amount"
        val bitmap = makeQrBitmap(payload)
        showQrDialog(bitmap)
        statusTv.text = "وضعیت: QR تولید شد"
    }

    private fun makeQrBitmap(text: String, size: Int = 640): Bitmap {
        val writer = QRCodeWriter()
        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
        hints[EncodeHintType.MARGIN] = 1
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size, hints)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
            }
        }
        return bmp
    }

    private fun showQrDialog(bmp: Bitmap) {
        val iv = ImageView(this).apply { setImageBitmap(bmp) }
        AlertDialog.Builder(this)
            .setTitle("QR خرید")
            .setView(iv)
            .setPositiveButton("بستن", null)
            .show()
    }

    private fun startBleDemo() {
        if (!hasBlePerms()) {
            requestBlePerms()
            return
        }
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            Toast.makeText(this, "BLE در این دستگاه پشتیبانی نمی‌شود", Toast.LENGTH_SHORT).show()
            return
        }
        if (!adapter.isEnabled) {
            Toast.makeText(this, "بلوتوث خاموش است", Toast.LENGTH_SHORT).show()
        } else {
            // اینجا بعداً تبلیغ BLE واقعی را اضافه می‌کنیم
            Toast.makeText(this, "BLE (فروشنده) شروع شد (دمو)", Toast.LENGTH_SHORT).show()
            statusTv.text = "وضعیت: BLE شروع شد"
        }
    }

    private fun stopBleDemo() {
        // اینجا بعداً توقف تبلیغ BLE واقعی را اضافه می‌کنیم
        Toast.makeText(this, "BLE متوقف شد (دمو)", Toast.LENGTH_SHORT).show()
        statusTv.text = "وضعیت: BLE متوقف شد"
    }

    private fun hasBlePerms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            BLE_PERMS_12P.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }
        } else {
            BLE_PERMS_LEGACY.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    private fun requestBlePerms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, BLE_PERMS_12P, REQ_BLE)
        } else {
            ActivityCompat.requestPermissions(this, BLE_PERMS_LEGACY, REQ_BLE)
        }
    }
}
