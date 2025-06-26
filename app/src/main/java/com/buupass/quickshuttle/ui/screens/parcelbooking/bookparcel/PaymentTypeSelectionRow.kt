package com.buupass.quickshuttle.ui.screens.parcelbooking.bookparcel

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.buupass.quickshuttle.utils.PaymentMethod

@Composable
fun PaymentTypeSelectionRow(
    paymentMethod: PaymentMethod,
    paymentMethods: List<PaymentMethod>?,
    onPaymentMethodSelected: (PaymentMethod) -> Unit,
) {
    var showPaymentTypeSelectionDialog by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Payment Method",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.8f)
                .padding(start = 16.dp)
        )
        Button(
            onClick = { showPaymentTypeSelectionDialog = true },
            shape = RoundedCornerShape(8.dp),
            enabled = false, // For now
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text(
                text = paymentMethod.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }

    // Show City To Selection Dialog
    if (showPaymentTypeSelectionDialog) {
        paymentMethods?.let { paymentTypes ->
            PaymentTypeSelectionDialog(
                paymentMethods = paymentTypes,
                onPaymentMethodSelected = { paymentMethod ->
                    onPaymentMethodSelected(paymentMethod)

                    showPaymentTypeSelectionDialog = false
                },
                onDismiss = {
                    showPaymentTypeSelectionDialog = false
                }
            )
        }
    }
}