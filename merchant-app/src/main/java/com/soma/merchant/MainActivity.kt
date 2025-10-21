package com.soma.merchant

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.soma.merchant.databinding.ActivityMainBinding
import com.soma.merchant.ble.BlePeripheralService
import com.soma.merchant.util.ReplayProtector
import shared.store.TxStore
import shared.utils.DateUtils
import shared.utils.QRHandler

class MainActivity : AppCompatActivity() {

    private lateinit var ui: ActivityMainBinding
    private lateinit var txStore: TxStore
    private lateinit var replayProtector: ReplayProtector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)

        txStore = TxStore(this)
        replayProtector = ReplayProtector(this)

        updateBalances()
        ui.statusView.text = "آماده دریافت تراکنش 🟢"

        ui.btnBleReceive.setOnClickListener {
            startService(Intent(this, BlePeripheralService::class.java))
            showStatus("BLE فعال شد و آماده دریافت است 💚", true)
        }

        ui.btnGenerateQr.setOnClickListener {
            val amountText = ui.etAmount.text.toString()
            if (amountText.isBlank()) {
                showStatus("مبلغ را وارد کنید", false)
                return@setOnClickListener
            }
            val amount = amountText.toLong()
            val txId = DateUtils.generateTxId()
            val payload = "$txId:$amount"
            val bmp: Bitmap = QRHandler.generate(payload)
            AlertDialog.Builder(this)
                .setTitle("QR برای تراکنش")
                .setMessage("کد تراکنش: $txId")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("باشه", null)
                .show()
            txStore.addTx(
                TxStore.Tx(
                    txId = txId,
                    amount = amount,
                    ts = System.currentTimeMillis(),
                    from = "buyer_qr",
                    to = "merchant",
                    method = "QR",
                    type = "main",
                    status = "SUCCESS"
                )
            )
            txStore.add("main", amount)
            updateBalances()
            showStatus("تراکنش QR ثبت شد ✅", true)
        }
    }

    private fun updateBalances() {
        val b = txStore.getBalances()
        ui.balanceView.text = "موجودی: ${"%,d".format(b.main)} تومان"
    }

    private fun showStatus(msg: String, success: Boolean) {
        ui.statusView.text = msg
        ui.statusView.setTextColor(if (success) 0xFF16A34A.toInt() else 0xFFDC2626.toInt())
    }
}
