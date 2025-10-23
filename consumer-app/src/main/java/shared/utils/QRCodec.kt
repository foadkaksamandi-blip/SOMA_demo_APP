package shared.utils

import android.util.Base64

/**
 * Encode/Decode پیام QR به Base64Url (بدون خط جدید).
 */
object QRCodec {
    fun encodeToQrText(payload: QRPayload): String {
        val json = payload.toJson().toByteArray(Charsets.UTF_8)
        return Base64.encodeToString(json, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    fun decodeFromQrText(qrText: String): QRPayload {
        val bytes = Base64.decode(qrText, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        val json = String(bytes, Charsets.UTF_8)
        return QRPayload.fromJson(json)
    }
}
