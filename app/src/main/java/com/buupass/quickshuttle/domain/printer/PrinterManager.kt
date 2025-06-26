package com.buupass.quickshuttle.domain.printer

import android.graphics.Bitmap
import com.buupass.quickshuttle.domain.parcel.ParcelReceiptDetails
import com.buupass.quickshuttle.domain.parcel.ParcelStickerDetails
import com.buupass.quickshuttle.ui.screens.common.printer.TicketDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface PrinterManager {
    val isConnected: StateFlow<Boolean>
    val scannedDevices: StateFlow<List<BluetoothDevice>>
    val pairedDevices: StateFlow<List<BluetoothDevice>>
    val connectedDevice: StateFlow<BluetoothDevice?>
    val errors: SharedFlow<String> // One tim event

    val navigateBackToBooking: SharedFlow<Unit>

    fun startDiscovery()

    fun stopDiscovery()

    fun startBluetoothServer(): Flow<ConnectionResult>

    fun connectToDevice(device: BluetoothDevice): Flow<ConnectionResult>

    fun closeConnection()

    // Frees up all resources associated with the printer manager
    fun release()

    fun printText(text: String, isBold: Boolean = false, isLeftAligned: Boolean = false)

    fun printBitMap(bitmap: Bitmap)

    suspend fun printPassengerTickets(ticketDetails: TicketDetails)

    suspend fun printParcelReceipt(parcelReceiptDetails: ParcelReceiptDetails?)

    suspend fun printParcelStickers(parcelStickerDetails: ParcelStickerDetails?)
}