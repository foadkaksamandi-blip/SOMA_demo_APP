package com.soma.merchant

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tv = TextView(this).apply {
            text = "SOMA Merchant (Demo)"
            textSize = 18f
            setPadding(48, 96, 48, 96)
        }
        setContentView(tv)
    }
}
