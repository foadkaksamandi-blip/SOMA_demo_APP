package com.soma.consumer.ble

import android.content.Context

class BleClient(private val context: Context) {

    interface Listener {
        fun onStatus(status: Status)
        fun onLog(msg: String)
        fun onTxResult(ok: Boolean)
    }

    enum class Status { READY, SCANNING, CONNECTING, CONNECTED, STOPPED, ERROR }

    private var listener: Listener? = null

    fun start(listener: Listener) {
        this.listener = listener
        listener.onStatus(Status.READY)
        listener.onLog("BLE initialized")
        // اینجا بعداً کُد واقعی BLE را وصل می‌کنیم
    }

    fun stop() {
        listener?.onStatus(Status.STOPPED)
        listener?.onLog("BLE stopped")
        listener = null
    }
}
