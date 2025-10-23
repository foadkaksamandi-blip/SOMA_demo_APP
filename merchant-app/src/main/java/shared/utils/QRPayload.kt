package shared.utils

import org.json.JSONObject

/**
 * پیام استاندارد داخل QR (بین دو اپ مشترک).
 */
data class QRPayload(
    val schema: String = "SOMA",
    val type: String = "PAY",
    val currency: String = "IRR",
    val amount: Long,            // مبلغ
    val txId: String,            // شناسه تراکنش
    val createdAt: String,       // تاریخ/زمان ایجاد (جلالی/نمایش)
    val merchantId: String = "SOMA-DEMO-MERCHANT" // دمو
) {
    fun toJson(): String {
        val obj = JSONObject()
            .put("schema", schema)
            .put("type", type)
            .put("currency", currency)
            .put("amount", amount)
            .put("txId", txId)
            .put("createdAt", createdAt)
            .put("merchantId", merchantId)
        return obj.toString()
    }

    companion object {
        fun fromJson(json: String): QRPayload {
            val o = JSONObject(json)
            return QRPayload(
                schema = o.optString("schema", "SOMA"),
                type = o.optString("type", "PAY"),
                currency = o.optString("currency", "IRR"),
                amount = o.getLong("amount"),
                txId = o.getString("txId"),
                createdAt = o.getString("createdAt"),
                merchantId = o.optString("merchantId", "SOMA-DEMO-MERCHANT")
            )
        }
    }
}
