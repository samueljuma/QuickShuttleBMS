package com.buupass.quickshuttle.data.printer

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.graphics.scale
import com.buupass.quickshuttle.domain.parcel.ParcelReceiptDetails
import com.buupass.quickshuttle.domain.parcel.ParcelStickerDetails
import com.buupass.quickshuttle.domain.printer.BluetoothDeviceDomain
import com.buupass.quickshuttle.domain.printer.ConnectionResult
import com.buupass.quickshuttle.domain.printer.PrinterManager
import com.buupass.quickshuttle.ui.screens.common.printer.TicketDetails
import com.buupass.quickshuttle.utils.BitmapHelper
import com.buupass.quickshuttle.utils.LOGO_PRINT_HEIGHT
import com.buupass.quickshuttle.utils.LOGO_PRINT_WIDTH
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class AndroidPrinterManager(
    private val context: Context
) : PrinterManager {

    val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }

    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter // could be null if device does not support bluetooth
    }

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean>
        get() = _isConnected.asStateFlow()

    private val _connectedDevice = MutableStateFlow<BluetoothDeviceDomain?>(null)
    override val connectedDevice: StateFlow<BluetoothDeviceDomain?>
        get() = _connectedDevice.asStateFlow()

    private val _errors = MutableSharedFlow<String>()
    override val errors: SharedFlow<String>
        get() = _errors.asSharedFlow()

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _pairedDevices.asStateFlow()

    private val _navigateBackToBooking = MutableSharedFlow<Unit>()
    override val navigateBackToBooking: SharedFlow<Unit>
        get() = _navigateBackToBooking.asSharedFlow()

    private val foundDeviceReceiver = FoundDeviceReceiver { device ->
        _scannedDevices.update { devices ->
            val newDevice = device.toBluetoothDeviceDomain()
            if (newDevice in devices) devices else devices + newDevice
        }
    }

    private val bluetoothStateReceiver = BluetoothStateReceiver { isConnected, bluetoothDevice ->
        if (bluetoothAdapter?.bondedDevices?.contains(bluetoothDevice) == true) {
            _isConnected.update { isConnected }
            _connectedDevice.update {
                bluetoothDevice.toBluetoothDeviceDomain()
            }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                _errors.emit("Can't connect to a non-paired device")
            }
        }
    }

    private var currentServerSocket: BluetoothServerSocket? = null // Not needed
    private var currentClientSocket: BluetoothSocket? = null // Only this is needed for this project

    init {
        updatePairedDevices()

        // register bluetooth state changed receiver here
        context.registerReceiver(
            bluetoothStateReceiver,
            IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
        )
    }

    override fun startDiscovery() {
        Log.d("AndroidPrinterManager", "Start Discovery Called")
        if (!hasRequiredBluetoothScanPermissions()) {
            Log.d("AndroidPrinterManager", "Permission not granted")
            return
        }

        Log.d("AndroidPrinterManager", "Permission granted")

        // register receiver here
        context.registerReceiver(
            foundDeviceReceiver,
            IntentFilter(BluetoothDevice.ACTION_FOUND)
        )

        updatePairedDevices()
        bluetoothAdapter?.startDiscovery()
        // Log paired devices
        Log.d("AndroidPrinterManager", "Paired Devices: ${_pairedDevices.value}")
    }

    override fun stopDiscovery() {
        Log.d("AndroidPrinterManager", "Stop Discovery Called")
        if (!hasRequiredBluetoothScanPermissions()) {
            return
        }

        bluetoothAdapter?.cancelDiscovery()
        // Log paired devices
        Log.d("AndroidPrinterManager", "Paired Devices: ${_pairedDevices.value}")
    }

    override fun startBluetoothServer(): Flow<ConnectionResult> {
        return flow {
            if (!hasRequiredBluetoothConnectPermissions()) {
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }
            currentServerSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                "QuickShuttleBMS",
                UUID.fromString(SERVICE_UUID)
            )

            var shouldLoop = true
            while (shouldLoop) {
                currentClientSocket = try {
                    currentServerSocket?.accept()
                } catch (e: IOException) {
                    shouldLoop = false
                    null
                }
                emit(ConnectionResult.ConnectionEstablished)
                currentClientSocket?.let {
                    currentServerSocket?.close()
                }
            }
        }.onCompletion {
//            closeConnection()
        }.flowOn(Dispatchers.IO) // Ensures we execute this on the IO thread
    }

    override fun connectToDevice(device: BluetoothDeviceDomain): Flow<ConnectionResult> {
        return flow {
            if (!hasRequiredBluetoothConnectPermissions()) {
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }

            val bluetoothDevice = bluetoothAdapter
                ?.getRemoteDevice(device.address)

            currentClientSocket = bluetoothDevice
                ?.createRfcommSocketToServiceRecord(
                    UUID.fromString(SERVICE_UUID)
                )
            stopDiscovery()

            if (bluetoothAdapter?.bondedDevices?.contains(bluetoothDevice) == false) {
            }

            currentClientSocket?.let { socket ->
                try {
                    socket.connect()
                    emit(ConnectionResult.ConnectionEstablished)
                    _connectedDevice.update {
                        BluetoothDeviceDomain(
                            name = device.name,
                            address = device.address
                        )
                    }

                    /*TODO Handle Socket Connection here*/
                } catch (e: IOException) {
                    socket.close()
                    currentClientSocket = null
                    emit(
                        ConnectionResult.Error(
                            message = "Couldn't connect to ${bluetoothDevice?.name} "
                        )
                    )
                }
            }
        }.onCompletion {
//            closeConnection()
        }.flowOn(Dispatchers.IO) // Ensures we execute this on the IO thread
    }

    override fun printText(
        text: String,
        isBold: Boolean,
        isLeftAligned: Boolean
    ) {
        val socket = currentClientSocket
        if (socket == null) {
            Log.d("AndroidPrinterManager", "Socket is null")
            return
        }

        try {
            // Reset formatting before printing
            socket.outputStream.write(byteArrayOf(27, 33, 0))
            socket.outputStream.write(byteArrayOf(27, 97, 1)) // Center alignment

            // Apply bold formatting if needed
            if (isBold) {
                if(connectedDevice.value?.name?.contains("CS30", ignoreCase = true) == true){
                    socket.outputStream.write(byteArrayOf(27, 69, 1)) // Enable bold
                }else{
                    socket.outputStream.write(byteArrayOf(27, 69, 1)) // Enable bold
                    socket.outputStream.write(byteArrayOf(29, 33, 16)) // Width scaling
                    socket.outputStream.write(byteArrayOf(27, 77, 1)) // Smaller font
                    socket.outputStream.write(byteArrayOf(27, 51, 35)) // Line spacing
                }
            }
            if (isLeftAligned) {
                socket.outputStream.write(byteArrayOf(27, 97, 0)) // Left alignment
            }

            // Print the string
            socket.outputStream.write(text.toByteArray())
        } catch (e: IOException) {
            Log.e("AndroidPrinterManager", "Print error", e)
        } finally {
            // Close the socket
//            closeConnection() // Not applicable, since it closes connection too early
        }
    }

    override fun printBitMap(bitmap: Bitmap) {
        val socket = currentClientSocket

        if (socket == null) {
            Log.d("AndroidPrinterManager", "Socket is null")
            return
        }

        try {
            val outputStream = socket.outputStream
            // ESC/POS default format before printing
            outputStream.write(byteArrayOf(27, 33, 0)) // Cancel bold
            outputStream.write(byteArrayOf(27, 97, 1)) // Center alignment
            outputStream.write(byteArrayOf(29, 119, 6)) // Set barcode width to 0.5mm
            outputStream.write(byteArrayOf(29, 104, 100)) // Set barcode height to 100 dots

            // Print the bitmap
            outputStream.write(BitmapHelper.decodeBitmap(bitmap))
        } catch (e: Exception) {
            Log.e("AndroidPrinterManager", "Print error", e)
        } finally {
            Log.d("AndroidPrinterManager", "Print Complete")
        }
    }

    override suspend fun printParcelReceipt(parcelReceiptDetails: ParcelReceiptDetails?) {
        if (parcelReceiptDetails == null) return
        val originalLogoBitMap = BitmapFactory.decodeResource(context.resources, parcelReceiptDetails.operatorDetails.logo)
        val resizedLogoBitMap = originalLogoBitMap.scale(LOGO_PRINT_WIDTH, LOGO_PRINT_HEIGHT, false)

        printBitMap(resizedLogoBitMap)
        printText(parcelReceiptDetails.operatorDetails.name, isBold = true)
        printText("\n")
        printText(parcelReceiptDetails.operatorDetails.contact1)
        printText(parcelReceiptDetails.operatorDetails.contact2)
        printText(parcelReceiptDetails.operatorDetails.contact3)
        printText(parcelReceiptDetails.operatorDetails.address)
        printText(parcelReceiptDetails.operatorDetails.location)
        printText("\n")
        printText("${parcelReceiptDetails.pickupLocation.uppercase()} TO ")
        printText("${parcelReceiptDetails.dropOffLocation.uppercase()}\n")
        printText("Date: ${parcelReceiptDetails.date} \n")
        printText("Sender: ${parcelReceiptDetails.senderName}\n", isLeftAligned = true)
        printText("Phone Number: ${parcelReceiptDetails.senderPhoneNUmber}\n", isLeftAligned = true)
        printText("Receiver: ${parcelReceiptDetails.receiverName}\n", isLeftAligned = true)
        printText("Phone Number: ${parcelReceiptDetails.receiverPhoneNumber}\n\n", isLeftAligned = true)
        for (item in parcelReceiptDetails.parcelItems) {
            printText("${item.quantity} Items: ${item.content} \n", isLeftAligned = true)
        }
        printText("Waybill No.: ${parcelReceiptDetails.waybill}\n", isLeftAligned = true)
        printText("Parcel REF.: ${parcelReceiptDetails.parcelRef}\n", isLeftAligned = true)
        printText("Total Cost Ksh. ${parcelReceiptDetails.totalCost}\n\n", isLeftAligned = true)
        printText("Served by ${parcelReceiptDetails.servedBy}\n")
        printText(parcelReceiptDetails.printedAt)
        printText(parcelReceiptDetails.tAndCs)
        printText(parcelReceiptDetails.poweredByString)
        printText("\n\n\n\n")
    }

    override suspend fun printParcelStickers(parcelStickerDetails: ParcelStickerDetails?) {
        if (parcelStickerDetails == null) return

        val parcelItems = parcelStickerDetails.parcelItems
        val originalLogoBitMap = BitmapFactory.decodeResource(context.resources, parcelStickerDetails.operatorDetails.logo)
        val resizedLogoBitMap = originalLogoBitMap.scale(LOGO_PRINT_WIDTH, LOGO_PRINT_HEIGHT, false)

        for (item in parcelItems) {
            val qrCode = BitmapHelper.encodeAsBitmap(item.parcel_item_code)
            printBitMap(resizedLogoBitMap)
            printText(parcelStickerDetails.operatorDetails.name, isBold = true)
            printText("\n")
            printText("${parcelStickerDetails.pickupLocation.uppercase()} TO ")
            printText("${parcelStickerDetails.dropOffLocation.uppercase()}\n")
            printText("Sender.: ${parcelStickerDetails.senderName}\n", isLeftAligned = true)
            printText("Phone Number.: ${parcelStickerDetails.senderPhoneNUmber}\n", isLeftAligned = true)
            printText("Receiver.: ${parcelStickerDetails.receiverName}\n", isLeftAligned = true)
            printText("Phone Number.: ${parcelStickerDetails.receiverPhoneNumber}\n", isLeftAligned = true)
            printText("Payment Type.: ${parcelStickerDetails.paymentType}\n", isLeftAligned = true)
            printText("Amount.: ${parcelStickerDetails.amount}\n", isLeftAligned = true)
            printBitMap(qrCode!!)
            printText("REF.: ${item.parcel_item_code}\n")
            printText("Waybill No.: ${item.waybill}\n")
            printText("\n\n\n\n")

        }
    }

    override suspend fun printPassengerTickets(ticketDetails: TicketDetails) {
        val passengers = ticketDetails.tripDetails?.passengers

        if (passengers.isNullOrEmpty()) return

        val originalLogoBitMap = BitmapFactory.decodeResource(context.resources, ticketDetails.operatorDetails.logo)
        val resizedLogoBitMap = originalLogoBitMap.scale(LOGO_PRINT_WIDTH, LOGO_PRINT_HEIGHT, false)
        val termsAndConditionsTitle = ticketDetails.termsAndConditions.first
        val termsAndConditions = ticketDetails.termsAndConditions.second

        for (passenger in passengers) {

            val qrCode = BitmapHelper.encodeAsBitmap(passenger.pnr)

            printBitMap(resizedLogoBitMap)
            printText(ticketDetails.operatorDetails.name, isBold = true)
            printText("\n")
            printText(ticketDetails.operatorDetails.contact1)
            printText(ticketDetails.operatorDetails.contact2)
            printText(ticketDetails.operatorDetails.contact3)
            printText(ticketDetails.operatorDetails.address)
            printText(ticketDetails.operatorDetails.location)
            printText("\n")
            printText("Ticket Number: ${ticketDetails.tripDetails.bookingID}\n", isLeftAligned = true)
            printText("PNR: ${passenger.pnr}\n", isLeftAligned = true)
            printText("Route: ${ticketDetails.tripDetails.route}\n", isLeftAligned = true)
            printText("Date: ${ticketDetails.tripDetails.date} ${ticketDetails.tripDetails.dayOfWeek}\n", isLeftAligned = true)
            printText("\n")
            printText("Reporting Time: ${ticketDetails.reportingTime}\n", isLeftAligned = true)
            printText("Departure Time: ${ticketDetails.departureTime}\n", isLeftAligned = true)
            printText("\n")
            printText("Name.......: ${passenger.name}\n", isLeftAligned = true)
            printText("Phone No...: ${passenger.phoneNumber}\n", isLeftAligned = true)
            printText("Amount.....: ${passenger.amount} \n", isLeftAligned = true)
            printText("Pick Up....: ${ticketDetails.tripDetails.pickUpPoint} \n", isLeftAligned = true)
            printText("Drop Off...: ${ticketDetails.tripDetails.dropOffPoint} \n", isLeftAligned = true)
            printText("Seat No....: ${passenger.seatNumber}\n", isLeftAligned = true)
            printText("\n")

//            printBitMap(qrCode!!)

            printText(termsAndConditionsTitle, isBold = true)
            termsAndConditions.forEach {
                printText(it, isLeftAligned = true)
            }
            printText("\n")
            ticketDetails.servedBy?.let {
                printText("Served By: $it\n")
            }
            ticketDetails.reprintedBy?.let {
                printText("Reprinted By: $it\n")
            }
            printText(ticketDetails.printedAt)

            printText(ticketDetails.poweredByString)

            printText("\n")
            if (!ticketDetails.reprintedBy.isNullOrEmpty()) {
                printText("NB - This is a Reprint Ticket\n")
            }

            printText("\n\n")
        }

        // on Done emit event to navigate back to booking screen
        if (ticketDetails.reprintedBy.isNullOrEmpty()) {
            _navigateBackToBooking.emit(Unit)
        }
    }

    override fun closeConnection() {
        currentClientSocket?.close()
        currentServerSocket?.close()
        currentClientSocket = null
        currentServerSocket = null
    }

    override fun release() {
        try{
            context.unregisterReceiver(foundDeviceReceiver)
            context.unregisterReceiver(bluetoothStateReceiver)
            closeConnection()
        }catch (e: Exception){
            Log.e("AndroidPrinterManager", "Error releasing resources", e)
        }
    }
    private fun updatePairedDevices() {
        Log.d("AndroidPrinterManager", "Update Paired Devices Called")

        if (!hasRequiredBluetoothConnectPermissions()) {
            return
        }

        bluetoothAdapter
            ?.bondedDevices // get all paired devices
            ?.map { it.toBluetoothDeviceDomain() }
            ?.also { devices ->
                Log.d("AndroidPrinterManager", "Paired Devices: $devices")
                _pairedDevices.update { devices }
            }
    }

    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasRequiredBluetoothConnectPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                Log.d("PermissionUtils", "BLUETOOTH_CONNECT permission not granted")
                false
            } else {
                true
            }
        } else {
            if (!hasPermission(Manifest.permission.BLUETOOTH) ||
                !hasPermission(Manifest.permission.BLUETOOTH_ADMIN)
            ) {
                Log.d("PermissionUtils", "Legacy Bluetooth permissions not granted")
                false
            } else {
                true
            }
        }
    }

    private fun hasRequiredBluetoothScanPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
                Log.d("PermissionUtils", "BLUETOOTH_CONNECT permission not granted")
                false
            } else {
                true
            }
        } else {
            if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d("PermissionUtils", "Legacy Bluetooth permissions not granted")
                false
            } else {
                true
            }
        }
    }

    companion object {
        const val SERVICE_UUID = "00001101-0000-1000-8000-00805F9B34FB"
    }
}

val ALL_BLE_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    arrayOf(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN
    )
} else {
    arrayOf(
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
}