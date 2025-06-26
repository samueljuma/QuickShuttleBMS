package com.buupass.quickshuttle.ui.screens.passengerbooking.booking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.buupass.quickshuttle.ui.screens.common.CustomTextField

@Composable
fun CustomerDetailsDialog(
    onDoneClicked: () -> Unit,
    onCancelClicked: () -> Unit,
    onAutoFillClicked: () -> Unit,
    onDismiss: () -> Unit,
    bookingScreenViewModel: BookingScreenViewModel,
    currency: String
) {
    val bookingUIState by bookingScreenViewModel.uiState.collectAsStateWithLifecycle()
    val freshCustomerDetails = bookingUIState.freshCustomerDetails
    val selectedSeat = bookingUIState.selectedSeat

    Dialog(
        onDismissRequest = { onDismiss() }
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = "Customer Details",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                // Customer Name
                CustomTextField(
                    value = freshCustomerDetails.customerName,
                    isError = freshCustomerDetails.customerNameError != null,
                    errorMessage = freshCustomerDetails.customerNameError,
                    onValueChange = {
                        bookingScreenViewModel.updateFreshCustomerDetails(
                            field = "customerName",
                            value = it
                        )
                    },
                    placeholder = "Name"
                )

                // Customer ID
                CustomTextField(
                    value = freshCustomerDetails.customerID,
                    isError = freshCustomerDetails.customerIDError != null,
                    errorMessage = freshCustomerDetails.customerIDError,
                    onValueChange = {
                        bookingScreenViewModel.updateFreshCustomerDetails(
                            field = "customerID",
                            value = it
                        )
                    },
                    placeholder = "ID Number/Passport Number",
                    keyboardType = KeyboardType.Number
                )

                // Customer Phone
                CustomTextField(
                    value = freshCustomerDetails.customerPhone,
                    isError = freshCustomerDetails.customerPhoneError != null,
                    errorMessage = freshCustomerDetails.customerPhoneError,
                    onValueChange = {
                        bookingScreenViewModel.updateFreshCustomerDetails(
                            field = "customerPhone",
                            value = it
                        )
                    },
                    placeholder = "Phone Number",
                    prefix = "+254",
                    keyboardType = KeyboardType.Number
                )

                // Amount to Pay
                CustomTextField(
                    value = freshCustomerDetails.amountToPay,
                    isError = freshCustomerDetails.amountToPayError != null,
                    errorMessage = freshCustomerDetails.amountToPayError,
                    onValueChange = {
                        bookingScreenViewModel.updateFreshCustomerDetails(
                            field = "amountToPay",
                            value = it,
                            seat = selectedSeat
                        )
                    },
                    placeholder = "Amount to Pay",
                    prefix = currency,
                    keyboardType = KeyboardType.Number
                )
                Spacer(modifier = Modifier.height(4.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    bookingUIState.selectedSchedule?.pickup_points?.let { pickupPoints ->
                        LocationPickerDropdown(
                            modifier = Modifier.padding(0.dp),
                            selectedLocation = bookingUIState.freshCustomerDetails.pickupPoint,
                            locations = pickupPoints,
                            onLocationSelected = { location ->
                                bookingScreenViewModel.upDateCustomerPickupOrDropOff(
                                    isPickUp = true,
                                    location = location
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    bookingUIState.selectedSchedule?.drop_off_points?.let { dropOffPoints ->
                        LocationPickerDropdown(
                            modifier = Modifier.padding(0.dp),
                            selectedLocation = bookingUIState.freshCustomerDetails.dropOffPoint,
                            locations = dropOffPoints,
                            onLocationSelected = { location ->
                                bookingScreenViewModel.upDateCustomerPickupOrDropOff(
                                    isPickUp = false,
                                    location = location
                                )
                            }
                        )
                    }
                }

                // Action Buttons Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            onAutoFillClicked()
                        }
                    ) {
                        Text(
                            text = "AUTOFILL",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Row {
                        TextButton(onClick = {
                            onCancelClicked()
                        }) {
                            Text(
                                text = "CANCEL",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        TextButton(
                            onClick = {
                                onDoneClicked()
                            }
                        ) {
                            Text(
                                text = "DONE",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun CustomerTypeSelector(
    customerType: String,
    onCustomerTypeChanged: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = customerType == "Normal",
                    onClick = { onCustomerTypeChanged("Normal") }
                )
                Text(
                    text = "Normal",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = customerType == "Loyal",
                    onClick = { onCustomerTypeChanged("Loyal") }
                )
                Text(
                    text = "Loyal",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerDropdown(
    modifier: Modifier = Modifier,
    selectedLocation: String,
    locations: List<String>,
    onLocationSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .menuAnchor()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = selectedLocation, modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                    contentDescription = "Dropdown"
                )
            }
        }

        ExposedDropdownMenu(
            modifier = modifier
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp)
                ),
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            locations.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option) },
                    onClick = {
                        expanded = false
                        onLocationSelected(option)
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location Icon",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
    }
}