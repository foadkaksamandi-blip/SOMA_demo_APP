package com.soma.consumer

import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.soma.consumer.databinding.ActivityMainBinding
import com.soma.consumer.ble.BleClient
import com.soma.consumer.qr.QrScanner
import shared.store.TxStore
import shared.utils.DateUtils

class MainActivity : AppCompatActivity() {

    private lateinit var ui: ActivityMainBinding
    private lateinit var txStore: TxStore
    private var bleClient: BleClient? = null
    private var scanner: QrScanner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)

        txStore = TxStore(this)
        bleClient = BleClient(this)
        scanner = QrScanner()

        updateBalances()
        ui.statusView.text = "آماده برای تراکنش آفلاین 🟢"

        ui.btnBlePay.setOnClickListener {
            val amountText = ui.etAmount.text.toString()
            if (amountText.isBlank()) {
                showStatus("لطفاً مبلغ را وارد کنید", false)
                return@setOnClickListener
            }
            val amount = amountText.toLong()
            if (!txStore.hasSufficient("main", amount)) {
                showStatus("موجودی کافی نیست ❌", false)
                return@setOnClickListener
            }

            val adapter = BluetoothAdapter.getDefaultAdapter()
            if (adapter == null || !adapter.isEnabled) {
                showStatus("بلوتوث فعال نیست ❌", false)
                return@setOnClickListener
            }

            val paired: Set<BluetoothDevice> = adapter.bondedDevices
            if (paired.isEmpty()) {
                showStatus("هیچ دستگاهی یافت نشد", false)
                return@setOnClickListener
            }

            val device = paired.first()
            bleClient?.connect(device) { ready ->
                if (ready) {
                    val txId = DateUtils.generateTxId()
                    bleClient?.send("$txId:$amount")
                    txStore.deduct("main", amount)
                    txStore.addTx(
                        TxStore.Tx(
                            txId = txId,
                            amount = amount,
                            ts = System.currentTimeMillis(),
                            from = "buyer",
                            to = device.name ?: "merchant",
                            method = "BLE",
                            type = "main",
                            status = "SUCCESS"
                        )
                    )
                    showStatus("پرداخت موفق ✅\nکد تراکنش: $txId", true)
                    updateBalances()
                } else showStatus("اتصال برقرار نشد ❌", false)
            }
        }

        ui.btnScanQr.setOnClickListener {
            showStatus("در حال اسکن QR...", true)
            // شبیه‌سازی برای تست: مبلغ را از EditText بخوانیم
            val amountText = ui.etAmount.text.toString()
            if (amountText.isBlank()) {
                showStatus("مبلغ نامعتبر است", false)
            } else {
                val amount = amountText.toLong()
                val txId = DateUtils.generateTxId()
                if (txStore.hasSufficient("main", amount)) {
                    txStore.deduct("main", amount)
                    txStore.addTx(
                        TxStore.Tx(
                            txId = txId,
                            amount = amount,
                            ts = System.currentTimeMillis(),
                            from = "buyer",
                            to = "merchant_qr",
                            method = "QR",
                            type = "main",
                            status = "SUCCESS"
                        )
                    )
                    showStatus("پرداخت QR موفق ✅\nکد: $txId", true)
                    updateBalances()
                } else showStatus("موجودی کافی نیست ❌", false)
            }
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
