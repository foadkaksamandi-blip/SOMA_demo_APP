package com.soma.merchant

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO:
        // وقتی امضای دقیق API کلاس BleServer مشخص شد (متدها و پارامترها),
        // اینجا دکمه‌ها را به متدهای واقعی مثل start/stop یا advertise/connect وصل می‌کنیم.
        // فعلاً عمداً هیچ ارجاعی به BleServer یا متدهای ناموجود نداریم تا بیلد قطعاً سبز شود.
    }
}
