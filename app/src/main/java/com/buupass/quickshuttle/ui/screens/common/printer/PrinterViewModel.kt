package com.buupass.quickshuttle.ui.screens.common.printer

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buupass.quickshuttle.domain.parcel.ParcelReceiptDetails
import com.buupass.quickshuttle.domain.parcel.ParcelStickerDetails
import com.buupass.quickshuttle.domain.printer.BluetoothDeviceDomain
import com.buupass.quickshuttle.domain.printer.ConnectionResult
import com.buupass.quickshuttle.domain.printer.PrinterManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PrinterViewModel(
    private val printerManager: PrinterManager
) : ViewModel() {
    private val _state = MutableStateFlow(PrinterUiState())
    val state = combine( // if either of these values changes we get the new values
        printerManager.scannedDevices,
        printerManager.pairedDevices,
        _state
    ) { scannedDevices, pairedDevices, state ->
        state.copy(
            scannedDevices = scannedDevices,
            pairedDevices = pairedDevices,
            allDevices = (scannedDevices + pairedDevices).toSet().toList()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

    // Get navigateBacktoBooking shared flow from printer manager
    val navigateBackToBooking = printerManager.navigateBackToBooking

    private val _printingState = MutableStateFlow<PrintingState>(PrintingState.Idle)
    val printingState: StateFlow<PrintingState> = _printingState.asStateFlow()

    private var deviceConnectionJob: Job? = null

    init {
        printerManager.isConnected.onEach { isConnected ->
            _state.update { it.copy(isConnected = isConnected) }
        }.launchIn(viewModelScope)

        printerManager.connectedDevice.onEach { device ->
            _state.update {
                it.copy(
                    selectedDevice = device
                )
            }
        }.launchIn(viewModelScope)

        printerManager.errors.onEach { error ->
            _state.update {
                it.copy(
                    errorMessage = error
                )
            }
        }.launchIn(viewModelScope)
    }

    // Start Scan
    fun startScan() {
        Log.d("PrinterViewModel", "Start Scanning Called ")
        printerManager.startDiscovery()
    }

    fun stopScan() {
        Log.d("PrinterViewModel", "Stop Scanning called ")
        printerManager.stopDiscovery()
    }

    fun connectToDevice(bluetoothDevice: BluetoothDeviceDomain) {
        _state.update {
            it.copy(
                selectedDevice = bluetoothDevice,
                isConnecting = true
            )
        }
        deviceConnectionJob = printerManager
            .connectToDevice(bluetoothDevice)
            .listen() // Extension function
    }

    fun resetPrinterDevicesToEmpty() {
        _state.update {
            it.copy(
                allDevices = emptyList()
            )
        }
    }

    fun updatePrintingStatus(isPrintingEnabled: Boolean) {
        if (!isPrintingEnabled) {
            // Clear UI state and release resources
            clearUiState()
        }
        _state.update {
            it.copy(
                printingIsTurnedOn = isPrintingEnabled
            )
        }
    }

    fun updateTicketDetails(ticketDetails: TicketDetails?) {
        _state.update {
            it.copy(
                ticketDetailsToPrint = ticketDetails
            )
        }
    }

    fun printPassengerTickets(ticketDetails: TicketDetails) {
        _state.update { it.copy(printingInProgress = true) }
        viewModelScope.launch {
            _printingState.value = PrintingState.Printing

            try {
                printerManager.printPassengerTickets(ticketDetails) // suspend function
                _printingState.value = PrintingState.Success
                clearTicketDetails()
            } catch (e: Exception) {
                _printingState.value = PrintingState.Error(e.message ?: "Error when printing")
            }finally {
                _state.update {
                    it.copy(
                        printingInProgress = false
                    )
                }
            }
        }
    }
    fun printParcelReceipt(parcelReceiptDetails: ParcelReceiptDetails?) {
        if (parcelReceiptDetails == null) return

        _state.update { it.copy(printingInProgress = true) }
        viewModelScope.launch {
            _printingState.value = PrintingState.Printing

            try {
                printerManager.printParcelReceipt(parcelReceiptDetails) // suspend function
                _printingState.value = PrintingState.Success

            } catch (e: Exception) {
                _printingState.value = PrintingState.Error(e.message ?: "Error when printing")
            }finally {
                _state.update {
                    it.copy(
                        printingInProgress = false
                    )
                }
            }
        }
    }

    fun printParcelStickers(parcelStickerDetails: ParcelStickerDetails?) {
        if (parcelStickerDetails == null) return

        _state.update { it.copy(printingInProgress = true) }
        viewModelScope.launch {
            _printingState.value = PrintingState.Printing

            try {
                printerManager.printParcelStickers(parcelStickerDetails) // suspend function
                _printingState.value = PrintingState.Success

            } catch (e: Exception) {
                _printingState.value = PrintingState.Error(e.message ?: "Error when printing")
            }finally {
                _state.update {
                    it.copy(
                        printingInProgress = false
                    )
                }
            }
        }
    }

    private fun clearTicketDetails() {
        _state.update {
            it.copy(
                ticketDetailsToPrint = null
            )
        }
    }

    /**
     * TODO Use this after finishing printing All receipts
     * */
    fun disconnectFromDevice() {
        deviceConnectionJob?.cancel()
        printerManager.closeConnection()
        _state.update {
            it.copy(
                isConnecting = false,
                isConnected = false
            )
        }
    }

    /**
     * Probably won't be needed here
     */
    fun waitForIncomingConnections() {
        _state.update { it.copy(isConnecting = true) }
        deviceConnectionJob = printerManager
            .startBluetoothServer()
            .listen()
    }

    fun updateAllPermissionsGranted() {
        _state.update {
            it.copy(
                allPermissionsGranted = true
            )
        }
        val isPrintingEnabled = _state.value.printingIsTurnedOn

        if (isPrintingEnabled) {
            _state.update { it.copy(showPrintersDialog = true) }
        }
    }

    fun updatePrinterStatusMessage(message: String) {
        _state.update {
            it.copy(
                printerStatusMessage = message
            )
        }
    }

    private fun Flow<ConnectionResult>.listen(): Job {
        return onEach { result ->
            when (result) {
                ConnectionResult.ConnectionEstablished -> {
                    _state.update {
                        it.copy(
                            printerStatusMessage = "Connected to ${_state.value.selectedDevice?.name}",
                            isConnected = true,
                            isConnecting = false,
                            errorMessage = null,
                            showPrintersDialog = false
                        )
                    }
                }
                is ConnectionResult.Error -> {
                    _state.update {
                        it.copy(
                            isConnected = false,
                            printerStatusMessage = result.message,
                            printingIsTurnedOn = false,
                            isConnecting = false,
                            errorMessage = result.message,
                            showPrintersDialog = false
                        )
                    }
                }
            }
        }.catch {
            printerManager.closeConnection()
            _state.update {
                it.copy(
                    isConnected = false,
                    isConnecting = false,
                    printingIsTurnedOn = false,
                    showPrintersDialog = false
                )
            }
        }.launchIn(viewModelScope)
    }

    fun setShowPrinterDialog(status: Boolean) {
        _state.update {
            it.copy(
                showPrintersDialog = status
            )
        }
    }

    fun turnOffPrinting() {
        _state.update {
            it.copy(
                printingIsTurnedOn = false,
                printerStatusMessage = "Not going to print"
            )
        }
    }

    fun clearUiState() {
        // Release resources
//        printerManager.release() //Too Early
        _state.update {
            PrinterUiState()
        }
    }

    override fun onCleared() {
        printerManager.release()
        clearUiState()
    }
}

sealed class PrintingState {
    object Idle : PrintingState()
    object Printing : PrintingState()
    object Success : PrintingState()
    data class Error(val message: String) : PrintingState()
}