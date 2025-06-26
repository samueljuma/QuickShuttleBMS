package com.buupass.quickshuttle.ui.screens.parcelbooking.bookparcel

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.buupass.quickshuttle.domain.parcel.ParcelItemDomain
import com.buupass.quickshuttle.ui.screens.common.CustomTextField


@Composable
fun AddParcelItem(
    onDoneClicked: () -> Unit,
    onCancelClicked: () -> Unit,
    onDismiss: () -> Unit,
    newParcelItem: ParcelItemDomain,
    onFieldChange: (field: String, value: String) -> Unit
) {
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
                    text = "Parcel Item Details",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                // Parcel Name
                CustomTextField(
                    value = newParcelItem.name,
                    isError = newParcelItem.nameError != null,
                    errorMessage = newParcelItem.nameError,
                    onValueChange = {
                        onFieldChange("parcelName", it)
                    },
                    placeholder = "Name"
                )
                //Parcel Quantity
                CustomTextField(
                    value = newParcelItem.quantity,
                    isError = newParcelItem.quantityError != null,
                    errorMessage = newParcelItem.quantityError,
                    keyboardType = KeyboardType.Number,
                    onValueChange = {
                        onFieldChange("parcelQuantity", it)
                    },
                    placeholder = "Quantity"
                )

                //Parcel Weight
                CustomTextField(
                    value = newParcelItem.weight,
                    isError = newParcelItem.weightError != null,
                    errorMessage = newParcelItem.weightError,
                    keyboardType = KeyboardType.Number,
                    onValueChange = {
                        onFieldChange("parcelWeight", it)
                    },
                    placeholder = "Weight"
                )

//                //Parcel Price
//                CustomTextField(
//                    value = newParcelItem.price,
//                    isError = newParcelItem.priceError != null,
//                    errorMessage = newParcelItem.priceError,
//                    keyboardType = KeyboardType.Number,
//                    onValueChange = {
//                        onFieldChange("parcelPrice", it)
//                    },
//                    placeholder = "Price"
//                )

                Spacer(modifier = Modifier.height(16.dp))
                // Action Buttons Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
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