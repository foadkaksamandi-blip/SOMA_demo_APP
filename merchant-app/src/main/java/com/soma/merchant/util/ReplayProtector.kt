package com.soma.merchant.util

import android.content.Context
import org.json.JSONArray

/**
 * ReplayProtector — جلوگیری از پذیرش دوبارهٔ همان txId
 * در فروشنده استفاده می‌شود: اگر txId دیده شده باشد → رد تراکنش.
 */
class ReplayProtector(context: Context) {

    private val prefs = context.getSharedPreferences("soma_store", Context.MODE_PRIVATE)

    fun acceptOnce(txId: String): Boolean {
        synchronized(this) {
            val arr = JSONArray(prefs.getString("tx_seen", "[]"))
            // اگر قبلاً بوده → رد
            for (i in 0 until arr.length()) {
                if (arr.getString(i) == txId) return false
            }
            // اضافه کن و ذخیره
            arr.put(txId)
            prefs.edit().putString("tx_seen", arr.toString()).apply()
            return true
        }
    }

    fun clearAll() {
        prefs.edit().remove("tx_seen").apply()
    }
}
