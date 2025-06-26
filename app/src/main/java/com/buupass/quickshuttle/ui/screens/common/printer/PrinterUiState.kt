package com.buupass.quickshuttle.ui.screens.common.printer

import com.buupass.quickshuttle.domain.printer.BluetoothDevice

data class PrinterUiState(
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    val allDevices: List<BluetoothDevice> = emptyList(),
    val isConnected: Boolean = false,
    val selectedDevice: BluetoothDevice? = null,
    val isConnecting: Boolean = false,
    val errorMessage: String? = null,
    val printingIsTurnedOn: Boolean = false,
    val printerStatusMessage: String = "Not going to print",
    val ticketDetailsToPrint: TicketDetails? = null,
    val allPermissionsGranted: Boolean = false,
    val showPrintersDialog: Boolean = false,
    val printingInProgress: Boolean = false
)