package com.soma.merchant.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object Perms {
    private const val REQ_BLE = 31
    private const val REQ_CAMERA = 32

    fun ensureBleScan(activity: Activity): Boolean {
        val needs = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= 31) {
            if (!granted(activity, Manifest.permission.BLUETOOTH_SCAN)) needs += Manifest.permission.BLUETOOTH_SCAN
            if (!granted(activity, Manifest.permission.BLUETOOTH_CONNECT)) needs += Manifest.permission.BLUETOOTH_CONNECT
        } else {
            if (!granted(activity, Manifest.permission.ACCESS_FINE_LOCATION)) needs += Manifest.permission.ACCESS_FINE_LOCATION
        }
        return requestIfNeeded(activity, needs, REQ_BLE)
    }

    fun ensureBleAdvertise(activity: Activity): Boolean {
        val needs = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= 31) {
            if (!granted(activity, Manifest.permission.BLUETOOTH_ADVERTISE)) needs += Manifest.permission.BLUETOOTH_ADVERTISE
            if (!granted(activity, Manifest.permission.BLUETOOTH_CONNECT)) needs += Manifest.permission.BLUETOOTH_CONNECT
        } else {
            if (!granted(activity, Manifest.permission.ACCESS_FINE_LOCATION)) needs += Manifest.permission.ACCESS_FINE_LOCATION
        }
        return requestIfNeeded(activity, needs, REQ_BLE)
    }

    fun ensureCamera(activity: Activity): Boolean {
        val needs = mutableListOf<String>()
        if (!granted(activity, Manifest.permission.CAMERA)) needs += Manifest.permission.CAMERA
        return requestIfNeeded(activity, needs, REQ_CAMERA)
    }

    private fun granted(a: Activity, p: String) =
        ContextCompat.checkSelfPermission(a, p) == PackageManager.PERMISSION_GRANTED

    private fun requestIfNeeded(a: Activity, needs: List<String>, code: Int): Boolean {
        if (needs.isEmpty()) return true
        ActivityCompat.requestPermissions(a, needs.toTypedArray(), code)
        return false
    }
}
