package com.soma.consumer.ble

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object BtPerms {
    private const val REQ = 2210

    fun ensure(activity: Activity): Boolean {
        val needs = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= 31) {
            if (!granted(activity, Manifest.permission.BLUETOOTH_SCAN)) needs += Manifest.permission.BLUETOOTH_SCAN
            if (!granted(activity, Manifest.permission.BLUETOOTH_CONNECT)) needs += Manifest.permission.BLUETOOTH_CONNECT
            if (!granted(activity, Manifest.permission.BLUETOOTH_ADVERTISE)) needs += Manifest.permission.BLUETOOTH_ADVERTISE
        } else {
            if (!granted(activity, Manifest.permission.BLUETOOTH)) needs += Manifest.permission.BLUETOOTH
            if (!granted(activity, Manifest.permission.BLUETOOTH_ADMIN)) needs += Manifest.permission.BLUETOOTH_ADMIN
        }
        return if (needs.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, needs.toTypedArray(), REQ)
            false
        } else true
    }

    private fun granted(a: Activity, p: String) =
        ContextCompat.checkSelfPermission(a, p) == PackageManager.PERMISSION_GRANTED
}
