package com.buupass.quickshuttle.ui.screens.parcelbooking.dispatchparcel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.buupass.quickshuttle.ui.screens.common.CustomTextField


@Composable
fun AddParcelDialog(
    onDoneClicked: () -> Unit,
    onCancelClicked: () -> Unit,
    onDismiss: () -> Unit,
    parcelCode : String,
    onParcelCodeChange: (String) -> Unit
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
                    .imePadding()
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = "Enter Parcel Code",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Parcel Name
                CustomTextField(
                    value = parcelCode,
                    onValueChange = {
                        onParcelCodeChange(it)
                    },
                    placeholder = "Parcel Code"
                )

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