package com.buupass.quickshuttle.ui.screens.parcelbooking.bookparcel


import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.buupass.quickshuttle.ui.screens.common.CustomAppBar
import com.buupass.quickshuttle.R
import com.buupass.quickshuttle.data.printer.ALL_BLE_PERMISSIONS
import com.buupass.quickshuttle.domain.parcel.ParcelItemDomain
import com.buupass.quickshuttle.domain.parcel.PayeePhoneNumber
import com.buupass.quickshuttle.domain.parcel.SenderReceiver
import com.buupass.quickshuttle.domain.parcel.TotalCost
import com.buupass.quickshuttle.ui.screens.common.CitySelectionRow
import com.buupass.quickshuttle.ui.screens.common.CustomButton
import com.buupass.quickshuttle.ui.screens.common.CustomTextField
import com.buupass.quickshuttle.ui.screens.common.LoadingDialog
import com.buupass.quickshuttle.ui.screens.common.PrinterStatusSection
import com.buupass.quickshuttle.ui.screens.common.RouteSelectionRow
import com.buupass.quickshuttle.ui.screens.common.printer.PrinterDialog
import com.buupass.quickshuttle.ui.screens.common.printer.PrinterViewModel
import com.buupass.quickshuttle.utils.PaymentMethod
import java.com.ctk.sdk.PosApiHelper

@Composable
fun BookParcelScreen(
    navController: NavController,
    bookParcelViewModel: BookParcelViewModel,
    printerViewModel: PrinterViewModel
) {

    val bookParcelUiState by bookParcelViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // For POS Printer
    val posApiHelper = PosApiHelper.getInstance()
    val posPrinterResult = posApiHelper.PrintInit()

    val sender = bookParcelUiState.senderDetails
    val receiver = bookParcelUiState.receiverDetails
    val selectedPaymentMethod = bookParcelUiState.selectedPaymentMethod
    val printerUiState by printerViewModel.state.collectAsStateWithLifecycle()
    val allDevices = printerUiState.allDevices.filter { !it.name.isNullOrBlank() }

    LoadingDialog(
        isLoading = bookParcelUiState.isLoading,
        message = bookParcelUiState.loadingMessage ?: "Loading..."
    )


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
    LaunchedEffect(key1 = printerUiState.printingIsTurnedOn) {
        if (printerUiState.printingIsTurnedOn) {
            printerViewModel.startScan()
        }
    }
    when {
        printerUiState.showPrintersDialog -> {
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
        bookParcelUiState.showAddParcelItemDialog ->{
            AddParcelItem(
                onDoneClicked = {
                    bookParcelViewModel.addParcelItem()
                },
                onCancelClicked = {
                    bookParcelViewModel.updateShowAddParcelItemDialog(false)
                },
                onDismiss = {
                    bookParcelViewModel.updateShowAddParcelItemDialog(false)
                },
                newParcelItem = bookParcelUiState.newParcelItem,
                onFieldChange = { field, value ->
                    bookParcelViewModel.updateNewParcelItemDetails(field, value)
                }
            )
        }

    }

    LaunchedEffect(Unit) {
        bookParcelViewModel.bookParcelEvent.collect { event ->
            when(event){
                is BookParcelEvent.ShowErrorMessageToast -> {
                    Toast.makeText(context, bookParcelUiState.errorMessage, Toast.LENGTH_SHORT).show()
                }
                is BookParcelEvent.ShowSuccessMessageToast -> {
                    Toast.makeText(context, bookParcelUiState.successMessage, Toast.LENGTH_SHORT).show()
                }else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Book Parcel",
                navigationIcon = R.drawable.arrow_back_ic,
                actionIcon = R.drawable.close_ic,
                onNavigationIconClick = { navController.navigateUp() },
                onActionIconClick = { navController.navigateUp() }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                RouteSelectionRow(
                    parcelRoute = bookParcelUiState.selectedParcelRoute,
                    parcelRoutes = bookParcelUiState.parcelRoutes,
                    onParcelRouteSelected = { route ->
                        bookParcelViewModel.onParcelRouteSelected(route)
                    }
                )
                CitySelectionRow(
                    city = bookParcelUiState.selectedPickupPoint,
                    isPickUp = true,
                    cityList = bookParcelUiState.selectedParcelRoute?.pickup_points,
                    onCitySelected = {
                        bookParcelViewModel.onPointSelected(it, isPickUp = true)
                    },
                    isParcelPointsSelection = true
                )
                CitySelectionRow(
                    city = bookParcelUiState.selectedDropOffPoint,
                    isPickUp = false,
                    cityList = bookParcelUiState.selectedParcelRoute?.dropoff_points,
                    onCitySelected = {
                        bookParcelViewModel.onPointSelected(it, isPickUp = false)
                    },
                    isParcelPointsSelection = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                SenderReceiverDetailsBlock(
                    senderReceiver = sender,
                    isSender = true,
                    onFieldChange = { field, value ->
                        bookParcelViewModel.updateSenderDetails(field, value)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                SenderReceiverDetailsBlock(
                    senderReceiver = receiver,
                    isSender = false,
                    onFieldChange = { field, value ->
                        bookParcelViewModel.updateReceiverDetails(field, value)
                    }
                )
                ParcelItemsBlock(
                    parcelItems = bookParcelUiState.parcelItems,
                    onAddParcelItemClicked = {
                        bookParcelViewModel.updateShowAddParcelItemDialog(true)
                    },
                    onRemoveItemClicked = {
                        bookParcelViewModel.removeParcelItem(it)
                    }
                )


                PaymentTypeSelectionRow(
                    paymentMethod = bookParcelUiState.selectedPaymentMethod,
                    paymentMethods = listOf(PaymentMethod.Mpesa, PaymentMethod.Cash),
                    onPaymentMethodSelected = {
                        bookParcelViewModel.onPaymentMethodSelected(it)
                    }
                )
                AnimatedVisibility(selectedPaymentMethod == PaymentMethod.Mpesa){
                    MpesaNumberRow(
                        payeePhoneNumber = bookParcelUiState.payeePhoneNumber,
                        onPhoneNumberChange = {
                            bookParcelViewModel.updatePayeePhoneNumber(it)
                        }
                    )
                }

                TotalCostRow(
                    totalCost = bookParcelUiState.totalCost,
                    onTotalCostChange = {
                        bookParcelViewModel.updateTotalCost(it)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                CustomButton(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = "BOOK PARCEL",
                    onClick = {
                        bookParcelViewModel.bookParcel()
                    }
                )
                PrinterStatusSection(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    isPrintingEnabled = printerUiState.printingIsTurnedOn,
                    onPrinterStatusToggle = {isEnabled ->
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
                CustomButton(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = "PRINT RECEIPT",
                    onClick = {
                        bookParcelViewModel.printParcelReceipt(
                            context = context,
                            posApiHelper = posApiHelper,
                            receiptDetails = bookParcelUiState.parcelReceiptDetails,
                            bluetoothIsOn = printerUiState.printingIsTurnedOn
                        )
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                CustomButton(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = "PRINT STICKER",
                    onClick = {
                        bookParcelViewModel.printParcelStickers(
                            bluetoothIsOn = printerUiState.printingIsTurnedOn,
                            context = context,
                            posApiHelper = posApiHelper,
                            stickerDetails = bookParcelUiState.parcelStickerDetails
                        )
                    }
                )

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    )
}
@Composable
fun SenderReceiverDetailsBlock(
    senderReceiver: SenderReceiver,
    isSender: Boolean,
    onFieldChange: (field: String, value: String) -> Unit
) {
    val title = if (isSender) "Sender's Details" else "Receiver's Details"

    Text(
        modifier = Modifier.fillMaxWidth(),
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        textAlign = TextAlign.Center
    )

    CustomTextField(
        modifier = Modifier.padding(horizontal = 16.dp),
        value = senderReceiver.name,
        placeholder = "Name",
        isError = senderReceiver.nameError != null,
        errorMessage = senderReceiver.nameError,
        onValueChange = { onFieldChange("name", it) }
    )
    if (isSender) {
        CustomTextField(
            modifier = Modifier.padding(horizontal = 16.dp),
            value = senderReceiver.kraPin,
            placeholder = "KRA PIN",
            isError = senderReceiver.kraPinError != null,
            errorMessage = senderReceiver.kraPinError,
            onValueChange = { onFieldChange("kraPin", it) }
        )
    }
    CustomTextField(
        modifier = Modifier.padding(horizontal = 16.dp),
        value = senderReceiver.idNumber,
        placeholder = if(isSender)"Sender ID" else "Receiver ID",
        keyboardType = KeyboardType.Number,
        isError = senderReceiver.idNumberError != null,
        errorMessage = senderReceiver.idNumberError,
        onValueChange = { onFieldChange("idNumber", it) }
    )
    CustomTextField(
        modifier = Modifier.padding(horizontal = 16.dp),
        value = senderReceiver.phoneNumber,
        prefix = "+254",
        keyboardType = KeyboardType.Number,
        isError = senderReceiver.phoneNumberError != null,
        errorMessage = senderReceiver.phoneNumberError,
        placeholder = "Phone Number",
        onValueChange = { onFieldChange("phoneNumber", it) }
    )
}


@Composable
fun ParcelItemsBlock(
    parcelItems: List<ParcelItemDomain>,
    onAddParcelItemClicked: () -> Unit,
    onRemoveItemClicked: (item: ParcelItemDomain) -> Unit
){
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Parcel Items",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
    }
    if (parcelItems.isEmpty()){
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = "No Parcel Items",
            tint = MaterialTheme.colorScheme.error,
        )
        Text(
            text = "No Parcel Items yet",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(16.dp)
        )
    }
    //This section will hold the list of parcel items
    parcelItems.forEachIndexed { index, parcelItem ->
        ParcelItem(
            index = index,
            parcelItem = parcelItem,
            onDeleteItem = {
                onRemoveItemClicked(parcelItem)
            }
        )
    }
    CustomButton(
        modifier = Modifier.padding(horizontal = 16.dp),
        text = "Add Parcel Item",
        onClick = onAddParcelItemClicked
    )
}

@Composable
fun ParcelItem(
    index: Int, parcelItem: ParcelItemDomain,
    onDeleteItem: () -> Unit
){
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Spacer(modifier = Modifier.width(10.dp))
        ParcelItemText(text = "${index + 1}.", modifier = Modifier.weight(0.2f))
        ParcelItemText(text = parcelItem.name, modifier = Modifier.weight(0.6f))
        ParcelItemText(text = parcelItem.quantity, modifier = Modifier.weight(0.2f))
        ParcelItemText(text = parcelItem.weight, modifier = Modifier.weight(0.2f))
        IconButton(
            onClick = onDeleteItem
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "Delete Parcel Item",
                tint = MaterialTheme.colorScheme.error
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
    }
}

@Composable
fun ParcelItemText(
    modifier: Modifier,
    text: String,
    color: Color = colorResource(R.color.green)
){
    Text(
        modifier = modifier,
        text = text,
        color = color,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun TotalCostRow(
    onTotalCostChange: (String) -> Unit,
    totalCost: TotalCost
){
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(
            text = "Total Cost",
            modifier = Modifier.weight(0.8f)
                .padding(start = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        CustomTextField(
            modifier = Modifier.weight(1f),
            value = totalCost.value,
            isError = totalCost.error != null,
            prefix = "Ksh",
            errorMessage = totalCost.error,
            keyboardType = KeyboardType.Number,
            textAlign = TextAlign.Center,
            onValueChange = {
                onTotalCostChange(it)
            }
        )
    }
}
@Composable
fun MpesaNumberRow(
    payeePhoneNumber: PayeePhoneNumber,
    onPhoneNumberChange: (String) -> Unit
){
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(
            text = "Phone Number",
            modifier = Modifier.weight(0.8f)
                .padding(start = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        CustomTextField(
            modifier = Modifier.weight(1f),
            value = payeePhoneNumber.value,
            isError = payeePhoneNumber.error != null,
            errorMessage = payeePhoneNumber.error,
            keyboardType = KeyboardType.Number,
            prefix = "+254",
            onValueChange = {
                onPhoneNumberChange(it)
            }
        )
    }
}



