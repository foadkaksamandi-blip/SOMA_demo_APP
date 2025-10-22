package com.soma.consumer.qr

import com.journeyapps.barcodescanner.ScanOptions

/**
 * فقط تنظیمات اسکن QR را می‌سازد.
 * لانچر (registerForActivityResult) باید داخل Activity ساخته شود.
 */
class QrScanner {
    fun options(): ScanOptions {
        val opts = ScanOptions()
        // فقط QR
        opts.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        // متن راهنما
        opts.setPrompt("کد QR را اسکن کنید")
        // بیپ
        opts.setBeepEnabled(true)
        // قفل جهت‌نمایی
        opts.setOrientationLocked(true)
        // دوربین پشت
        // opts.setCameraId(0) // در صورت نیاز فعال کن
        return opts
    }
}
