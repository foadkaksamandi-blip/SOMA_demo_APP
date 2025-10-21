package com.soma.merchant

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // نمونه: دکمه‌ها را بعداً به BLE/QR وصل می‌کنیم
        findViewById<Button>(R.id.btnPay).setOnClickListener {
            // TODO: شروع تراکنش
        }
        findViewById<Button>(R.id.btnRefund).setOnClickListener {
            // TODO: مرجوعی
        }
        findViewById<Button>(R.id.btnReport).setOnClickListener {
            // TODO: گزارش
        }
    }
}
