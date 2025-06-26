package com.buupass.quickshuttle.data.printer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.buupass.quickshuttle.domain.printer.BluetoothDeviceDomain

@SuppressLint("MissingPermission") // Suppress since we will add all permission checks in the printer Manager
fun BluetoothDevice.toBluetoothDeviceDomain(): BluetoothDeviceDomain {
    return BluetoothDeviceDomain(
        name = name,
        address = address
    )
}