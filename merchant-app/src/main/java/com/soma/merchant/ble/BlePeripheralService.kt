package com.soma.merchant.ble

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.ParcelUuid  // << مهم

class BlePeripheralService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
    // بدنه‌ی سرویس را بعداً کامل می‌کنیم؛ فعلاً برای کامپایل کافی است.
}
