package com.buupass.quickshuttle.ui.screens.reprintticket

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.buupass.quickshuttle.R
import com.buupass.quickshuttle.data.printer.ALL_BLE_PERMISSIONS
import com.buupass.quickshuttle.ui.screens.common.CustomAppBar
import com.buupass.quickshuttle.ui.screens.common.LoadingDialog
import com.buupass.quickshuttle.ui.screens.common.MessageDialog
import com.buupass.quickshuttle.ui.screens.common.PrinterStatusSection
import com.buupass.quickshuttle.ui.screens.common.printer.PrinterDialog
import com.buupass.quickshuttle.ui.screens.common.printer.PrinterViewModel


@Composable
fun ReprintTicketScreen(
    navController: NavController,
    reprintTicketViewModel: ReprintTicketViewModel,
    printerViewModel: PrinterViewModel
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val coroutineScope = rememberCoroutineScope()

    val reprintTicketScreenUIState by reprintTicketViewModel.uiState.collectAsStateWithLifecycle()

    val isLoading = reprintTicketScreenUIState.isLoading
    val showTicketFetchStatusDialog = reprintTicketScreenUIState.showSuccessTicketFetchDialog
    val loadingDialogMessage = reprintTicketScreenUIState.loadingDialogMessage
    val ticketDetails = reprintTicketScreenUIState.ticketDetails
    val errorMessage = reprintTicketScreenUIState.errorMessage
    val bookingID = reprintTicketScreenUIState.bookingId

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

    val printerUiState by printerViewModel.state.collectAsStateWithLifecycle()
    val allDevices = printerUiState.allDevices.filter { !it.name.isNullOrBlank() }
    val printingIsTurnedOn = printerUiState.printingIsTurnedOn
    val showPrintersDialog = printerUiState.showPrintersDialog
    val showErrorMessageDialog = reprintTicketScreenUIState.showErrorMessageDialog

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
        showTicketFetchStatusDialog -> {
            SuccessTicketFetchDialog(
                printingIsOn = printingIsTurnedOn,
                onButtonClick = {
                    reprintTicketViewModel.resetTicketFetchStatusDialog()
                    if (printingIsTurnedOn) {
                        ticketDetails?.let { ticketDetails ->
                            printerViewModel.printPassengerTickets(ticketDetails)

                            // Clear the ticket details
                            reprintTicketViewModel.clearTicketDetails()
                        }
                    }
                },
                onDismiss = {
                    reprintTicketViewModel.resetTicketFetchStatusDialog()
                }
            )
        }
        showErrorMessageDialog -> {
            MessageDialog(
                onDismiss = { reprintTicketViewModel.resetShowErrorMessageDialog() },
                dialogTitle = "Oops! Error!",
                dialogText = errorMessage ?: "Something went wrong",
                icon = Icons.Outlined.ErrorOutline,
                isErrorMessage = true
            )
        }
    }

    LoadingDialog(
        isLoading = isLoading,
        message = loadingDialogMessage,
        onDismiss = {
            /**TODO*/
        }
    )

    LaunchedEffect(key1 = printingIsTurnedOn) {
        if (printingIsTurnedOn) {
            printerViewModel.startScan()
        }
    }

    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Reprint Ticket",
                navigationIcon = R.drawable.arrow_back_ic,
                actionIcon = R.drawable.close_ic,
                onActionIconClick = {
                    onNavigateBack(
                        navController,
                        reprintTicketViewModel,
                        printerViewModel,
                        printingIsTurnedOn
                    )
                },
                onNavigationIconClick = {
                    onNavigateBack(
                        navController,
                        reprintTicketViewModel,
                        printerViewModel,
                        printingIsTurnedOn
                    )
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextField(
                        value = bookingID,
                        onValueChange = { bookingID ->
                            reprintTicketViewModel.updateBookingId(bookingID)
                            // Clear the ticket reprint UI state
                        },
                        placeholder = {
                            Text(
                                text = "Enter Booking ID",
                                color = Color.Gray
                            )
                        },
                        modifier = Modifier
                            .border(
                                BorderStroke(
                                    width = if (isFocused) 2.dp else 1.dp,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .weight(1f)
                            .background(Color.White, RoundedCornerShape(8.dp)),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        colors = TextFieldDefaults.colors(
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        interactionSource = interactionSource
                    )
                    Button(
                        onClick = {
                            keyboardController?.hide() // Hide keyboard
                            // Search for booking ID
                            reprintTicketViewModel.fetchTicketDetails()
                        },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .height(54.dp),
                        shape = RoundedCornerShape(6.dp),
                        enabled = bookingID.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            disabledContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = "SEARCH",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

    )
}

@Composable
fun SuccessTicketFetchDialog(
    onButtonClick: () -> Unit,
    onDismiss: () -> Unit,
    printingIsOn: Boolean
) {
    Dialog(
        onDismissRequest = { onDismiss() }
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier.size(40.dp),
                    imageVector = Icons.Default.Done,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    text = "Success",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    text = "Booking Retrieved!",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                )

                if (printingIsOn) {
                    Button(
                        onClick = { onButtonClick() },
                        modifier = Modifier
                            .padding(vertical = 10.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Reprint",
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    TextButton(
                        modifier = Modifier.align(Alignment.End),
                        onClick = { onButtonClick() }
                    ) {
                        Text(
                            text = "Ok",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

fun onNavigateBack(
    navController: NavController,
    reprintTicketViewModel: ReprintTicketViewModel,
    printerViewModel: PrinterViewModel,
    isPrinterTurnedOn: Boolean
) {
    navController.popBackStack()
    reprintTicketViewModel.clearUiState()
    printerViewModel.clearUiState()
    if (isPrinterTurnedOn) {
        printerViewModel.disconnectFromDevice()
    }
}