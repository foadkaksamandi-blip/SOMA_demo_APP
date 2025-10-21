package com.soma.merchant

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.soma.merchant.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var ui: ActivityMainBinding
    private var balanceMain = 5_000_000L
    private var balanceSubsidy = 0L
    private var balanceEmergency = 0L
    private var balanceCBDC = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)

        // مقداردهی اولیه
        ui.balanceView.text = "موجودی: ${"%,d".format(balanceMain)} تومان"
        ui.statusView.text = getString(R.string.status_ready_ble)

        // رویدادهای دکمه‌ها (منطق BLE/QR در فازهای بعدی متصل می‌شود)
        ui.btnBleReceive.setOnClickListener {
            // مرحله ۴: شروع سرویس BLE و انتظار برای پرداخت
            ui.statusView.text = getString(R.string.status_ready_ble)
        }

        ui.btnGenerateQr.setOnClickListener {
            // مرحله ۴: نمایش QR از مبلغ داخل etAmount
            if (ui.etAmount.text.isNullOrBlank()) {
                ui.statusView.text = getString(R.string.status_enter_amount)
            } else {
                ui.statusView.text = "QR آماده نمایش است"
            }
        }

        // سکشن‌های ویژه (آینده: اتصال به نوع کیف و QR/BLE اختصاصی)
        ui.btnCbdcBle.setOnClickListener { ui.statusView.text = "BLE — رمز ارز ملی" }
        ui.btnCbdcQr.setOnClickListener { ui.statusView.text = "QR — رمز ارز ملی" }
        ui.btnSubsidyBle.setOnClickListener { ui.statusView.text = "BLE — یارانه" }
        ui.btnSubsidyQr.setOnClickListener { ui.statusView.text = "QR — یارانه" }
        ui.btnEmergencyBle.setOnClickListener { ui.statusView.text = "BLE — اضطراری" }
        ui.btnEmergencyQr.setOnClickListener { ui.statusView.text = "QR — اضطراری" }
    }
}
