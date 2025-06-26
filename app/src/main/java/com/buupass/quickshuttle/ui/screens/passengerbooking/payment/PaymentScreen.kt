package com.buupass.quickshuttle.ui.screens.passengerbooking.payment

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.buupass.quickshuttle.R
import com.buupass.quickshuttle.data.printer.ALL_BLE_PERMISSIONS
import com.buupass.quickshuttle.utils.validatePhone
import com.buupass.quickshuttle.navigation.AppScreens
import com.buupass.quickshuttle.ui.screens.passengerbooking.booking.BookingScreenViewModel
import com.buupass.quickshuttle.ui.screens.common.CustomAppBar
import com.buupass.quickshuttle.ui.screens.common.CustomButton
import com.buupass.quickshuttle.ui.screens.common.CustomOutlinedTextField
import com.buupass.quickshuttle.ui.screens.common.LoadingDialog
import com.buupass.quickshuttle.ui.screens.common.MessageDialog
import com.buupass.quickshuttle.ui.screens.common.PrinterStatusSection
import com.buupass.quickshuttle.ui.screens.common.printer.PrinterDialog
import com.buupass.quickshuttle.ui.screens.common.printer.PrinterViewModel
import com.buupass.quickshuttle.utils.PaymentMethod
import com.buupass.quickshuttle.utils.color
import com.buupass.quickshuttle.utils.generateValidPhoneNumber
import com.buupass.quickshuttle.utils.printPassengerTicket
import java.com.ctk.sdk.PosApiHelper

@Composable
fun PaymentScreen(
    navController: NavController,
    printerViewModel: PrinterViewModel,
    paymentsViewModel: PaymentsViewModel,
    bookingScreenViewModel: BookingScreenViewModel
) {
    val currency = paymentsViewModel.getCurrentUser().currency

    val context = LocalContext.current
    var paymentMethod by remember { mutableStateOf(PaymentMethod.Cash) }
    var phoneNumber by remember { mutableStateOf("") } // TODO Remove before prod

    // TODO Also for dev - Remove before prod
    LaunchedEffect(key1 = Unit) {
        paymentsViewModel.updateBookingRequest(
            property = FieldsToUpdate.PayeePhone,
            value = phoneNumber.generateValidPhoneNumber()
        )
    }

    // For POS Printer
    val posApiHelper = PosApiHelper.getInstance()
    posApiHelper.PrintInit()

    var phoneNumberError by remember { mutableStateOf("") }

    val bluetoothManager = remember {
        context.getSystemService(BluetoothManager::class.java)
    }
    val bluetoothAdapter = bluetoothManager?.adapter
    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Optionally handle result
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val allPermissionsGranted = perms.all { it.value }

        if (allPermissionsGranted) {
            val canEnableBluetooth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                perms[Manifest.permission.BLUETOOTH_CONNECT] == true
            } else {
                true
            }

            if (canEnableBluetooth && bluetoothAdapter?.isEnabled == false) {
                enableBluetoothLauncher.launch(
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                )
            }
            // update permissions  granted to true
            printerViewModel.updateAllPermissionsGranted()
        } else {
            // Handle permission denied
        }
    }

    val payBtnEnabled = when (paymentMethod) {
        PaymentMethod.Cash -> true
        PaymentMethod.Mpesa -> phoneNumber.isNotBlank() && phoneNumberError.isEmpty()
    }

    val printerUiState by printerViewModel.state.collectAsStateWithLifecycle()
    val allDevices = printerUiState.allDevices.filter { !it.name.isNullOrBlank() }
    val printingIsTurnedOn = printerUiState.printingIsTurnedOn
    val showPrintersDialog = printerUiState.showPrintersDialog
    val printingIsInProgress = printerUiState.printingInProgress

    val paymentsUiState by paymentsViewModel.uiState.collectAsStateWithLifecycle()
    val totalFare = paymentsUiState.initiateBookingRequest?.total_fare ?: "0"

    val isLoading = paymentsUiState.isLoading
    val showMpesaConfirmationDialog = paymentsUiState.showMpesaConfirmationDialog
    val showErrorMessageDialog = paymentsUiState.showErrorMessageDialog
    val showSuccessMessageDialog = paymentsUiState.showSuccessMessageDialog
    val paymentProcessed = paymentsUiState.paymentProcessed

    val bookingUiState by bookingScreenViewModel.uiState.collectAsStateWithLifecycle()
    val selectedSchedule = bookingUiState.selectedSchedule

    when {
        showPrintersDialog -> {
            PrinterDialog(
                printerViewModel = printerViewModel,
                bluetoothDevices = allDevices,
                onDeviceSelected = { device ->
                    printerViewModel.connectToDevice(device)
                },
                onDismiss = {
                    printerViewModel.setShowPrinterDialog(false)
                    printerViewModel.turnOffPrinting()
                }
            )
        }
        printingIsInProgress -> {
            LoadingDialog(
                isLoading = printingIsInProgress,
                message = "Printing ...",
            )
        }

        showMpesaConfirmationDialog -> {
            MpesaConfirmationDialog(
                onCancel = paymentsViewModel::resetShowMpesaConfirmationDialog,
                onConfirm = {
                    if (selectedSchedule != null) {
                        paymentsViewModel.confirmMpesaPayment(selectedSchedule)
                    }
                },
                dialogTitle = "Confirm Mpesa Payment",
                dialogText = "Check Customer Mpesa Message then Click CONFIRM",
                icon = Icons.Outlined.Info
            )
        }
        showErrorMessageDialog -> {
            MessageDialog(
                onDismiss = paymentsViewModel::resetShowErrorMessageDialog,
                dialogTitle = "Oops! Error!",
                dialogText = paymentsUiState.errorMessage ?: "Unknown Error",
                icon = Icons.Outlined.ErrorOutline,
                isErrorMessage = true
            )
        }
    }

    LaunchedEffect(key1 = printingIsTurnedOn) {
        if (printingIsTurnedOn) {
            printerViewModel.startScan()
        }
    }

    LaunchedEffect(printerUiState.ticketDetailsToPrint) {
        printerUiState.ticketDetailsToPrint?.let { ticketDetails ->

            val posPrinterReady = try {
                posApiHelper.PrintCheckStatus() == 0
            } catch (e: Exception) {
                Log.e("POSCheck", "POS status check failed: ${e.localizedMessage}")
                false
            }

            if (posPrinterReady) {
                try {
                    printPassengerTicket(
                        posApiHelper =   posApiHelper,
                        ticketDetails = ticketDetails,
                        context = context
                    )
                    paymentsViewModel.triggerNavigationToBooking()
                } catch (e: Exception) {
                    Log.e("POSPrint", "POS SDK print failed: ${e.localizedMessage}")
                    // If POS fails during printing, fallback to Bluetooth
                    if (printingIsTurnedOn) {
                        printerViewModel.printPassengerTickets(ticketDetails)
                        paymentsViewModel.triggerNavigationToBooking()
                    } else if (paymentProcessed) {
                        Toast.makeText(context, "Printer is not turned on", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // POS is not ready at all, use Bluetooth
                if (printingIsTurnedOn) {
                    printerViewModel.printPassengerTickets(ticketDetails)
                    paymentsViewModel.triggerNavigationToBooking()
                } else if (paymentProcessed) {
                    Toast.makeText(context, "Printer is not turned on", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        paymentsViewModel.paymentEvent.collect{ event ->
            when(event){
                is PaymentEvent.ShowErrorMessageToast ->{
                    Toast.makeText(context, paymentsUiState.errorMessage, Toast.LENGTH_SHORT).show()
                }
                is PaymentEvent.ShowSuccessMessageToast ->{
                    Toast.makeText(context, paymentsUiState.successMessage, Toast.LENGTH_SHORT).show()
                }
                is PaymentEvent.NavigateBackToBookingScreen ->{
                    navController.navigate(AppScreens.BookingScreen.route) {
                        popUpTo(AppScreens.BookingScreen.route) {
                            inclusive = false
                        }
                    }
                    // Clear UI States
                    paymentsViewModel.clearUIState()
                    printerViewModel.clearUiState()
                    bookingScreenViewModel.clearUIState()
                }
            }
        }
    }

    LaunchedEffect(key1 = paymentsUiState.ticketDetails) {
        printerViewModel.updateTicketDetails(paymentsUiState.ticketDetails)
    }

    LoadingDialog(
        isLoading = isLoading,
        message = paymentsUiState.loadingMessage ?: "Processing ...",
        onDismiss = {}
    )

    // Handle back press
    BackHandler {
        if (paymentProcessed) {
            // navigate all the way to Booking Screen again
            navController.popBackStack(
                AppScreens.BookingScreen.route,
                inclusive = false
            )

            // Clear UI States
            paymentsViewModel.clearUIState()
            printerViewModel.clearUiState()
            bookingScreenViewModel.clearUIState()
        } else {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Payment",
                navigationIcon = R.drawable.arrow_back_ic,
                actionIcon = R.drawable.more_action_ic,
                actionIconIsMoreVert = true,
                menuItems = listOf(
                    "Confirm Mpesa" to {
                        // if payment method is mpesa, confirm mpesa payment
                        if (paymentsUiState.bookingIDForMpesaConfirmation != null) {
                            if (selectedSchedule != null) {
                                paymentsViewModel.confirmMpesaPayment(selectedSchedule)
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "No Mpesa Payments to confirm",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                ),
                onNavigationIconClick = {
                    if (paymentProcessed) {
                        // navigate all the way to Booking Screen again
                        navController.popBackStack(
                            AppScreens.BookingScreen.route,
                            inclusive = false
                        )

                        // Clear UI States
                        paymentsViewModel.clearUIState()
                        printerViewModel.clearUiState()
                        bookingScreenViewModel.clearUIState()
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Payment Methods Row
                PaymentMethodsRow(
                    onCashPaymentBtnClick = {
                        paymentMethod = PaymentMethod.Cash
                        paymentsViewModel.updateBookingRequest(
                            property = FieldsToUpdate.PaymentType,
                            value = paymentMethod.name
                        )
                    },
                    onMpesaPaymentBtnClick = {
                        paymentMethod = PaymentMethod.Mpesa
                        paymentsViewModel.updateBookingRequest(
                            property = FieldsToUpdate.PaymentType,
                            value = paymentMethod.name
                        )
                    }
                )

                // Amount to Pay Text Field
                CustomOutlinedTextField(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    text = totalFare,
                    readOnly = true,
                    prefix = currency
                )

                // Phone Number Text Field
                AnimatedVisibility(visible = paymentMethod == PaymentMethod.Mpesa) {
                    CustomOutlinedTextField(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        text = phoneNumber,
                        isError = phoneNumberError.isNotEmpty(),
                        placeHolder = "Enter Phone Number",
                        onValueChange = { newText ->
                            phoneNumber = newText

                            phoneNumberError = validatePhone(newText) ?: ""

                            if (phoneNumberError.isEmpty()) {
                                // Update Booking Request
                                paymentsViewModel.updateBookingRequest(
                                    property = FieldsToUpdate.PayeePhone,
                                    value = phoneNumber.generateValidPhoneNumber()
                                )
                            }
                        },
                        keyboardType = KeyboardType.Number,
                        prefix = "+254"
                    )
                }
                AnimatedVisibility(
                    visible = phoneNumberError.isNotEmpty(),
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = phoneNumberError,
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.error
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pay Button
                CustomButton(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    text = "PAY",
                    enabled = payBtnEnabled,
                    onClick = {
                        // TODO: Handle Payment
                        if (paymentProcessed) {
                            Toast.makeText(context, "You have already made this booking", Toast.LENGTH_SHORT).show()
                        } else {
                            if (selectedSchedule != null) {
                                paymentsViewModel.initiateBooking(
                                    selectedSchedule
                                )
                            }
                        }
                    },
                    buttonColor = paymentMethod.color()

                )

                Spacer(modifier = Modifier.height(24.dp))

                // Printer Status Section
                PrinterStatusSection(
                    isPrintingEnabled = printingIsTurnedOn,
                    onPrinterStatusToggle = { isEnabled ->
                        // Request Permissions
                        requestPermissionLauncher.launch(
                            ALL_BLE_PERMISSIONS
                        )

                        // first clear status
                        printerViewModel.updatePrinterStatusMessage("Connecting ...")

                        // Then update status
                        printerViewModel.updatePrintingStatus(isEnabled)

                        // if not enabled, update status
                        if (!isEnabled) {
                            // clear ui state and release resources
                            printerViewModel.clearUiState()
                            printerViewModel.updatePrinterStatusMessage("Not going to print")
                        }
                    },
                    state = printerUiState
                )

                Spacer(modifier = Modifier.height(24.dp))
                CustomButton(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    enabled = printerUiState.ticketDetailsToPrint != null && printerUiState.printingIsTurnedOn,
                    text = "PRINT TICKET",
                    onClick = {
                        printerUiState.ticketDetailsToPrint?.let {
                            printerViewModel.printPassengerTickets(it)
                        }
                    },
                    buttonColor = MaterialTheme.colorScheme.primary
                )
            }
        }

    )
}

@Composable
fun PaymentMethodsRow(
    onCashPaymentBtnClick: () -> Unit,
    onMpesaPaymentBtnClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CustomButton(
            modifier = Modifier.weight(1f),
            buttonColor = PaymentMethod.Cash.color(),
            text = PaymentMethod.Cash.name.uppercase(),
            onClick = { onCashPaymentBtnClick() }
        )
        CustomButton(
            modifier = Modifier.weight(1f),
            enabled = false,
            buttonColor = PaymentMethod.Mpesa.color(),
            text = PaymentMethod.Mpesa.name.uppercase(),
            onClick = { onMpesaPaymentBtnClick() }
        )
    }
}
