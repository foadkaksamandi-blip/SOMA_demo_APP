package com.soma.merchant

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.soma.merchant.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.textView.text = "Merchant App is running"
    }
}
