package com.soma.merchant.shared

import java.util.UUID

object Protocol {
    val SERVICE_UUID: UUID = UUID.fromString("000050ma-0000-1000-8000-00805f9b34fb")
    val CHAR_CMD_UUID: UUID = UUID.fromString("0000c0de-0000-1000-8000-00805f9b34fb")
    val CHAR_RESULT_UUID: UUID = UUID.fromString("0000r3s0-0000-1000-8000-00805f9b34fb")

    fun newTxId(): String = UUID.randomUUID().toString().substring(0, 8).uppercase()
}
