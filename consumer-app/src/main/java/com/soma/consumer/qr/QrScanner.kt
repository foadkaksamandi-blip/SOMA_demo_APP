package com.soma.consumer.qr

import android.app.Activity

object QrScanner {
    fun startScan(
        activity: Activity,
        onResult: (String) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        // اگر قبلاً پیاده‌سازی‌ داری، همان را نگه‌دار.
        onError(UnsupportedOperationException("QR implementation is external in this build"))
    }
}
