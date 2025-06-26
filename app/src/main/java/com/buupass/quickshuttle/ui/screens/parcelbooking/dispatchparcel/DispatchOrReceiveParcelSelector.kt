package com.buupass.quickshuttle.ui.screens.parcelbooking.dispatchparcel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DispatchOrReceiveParcelSelector(
    actionType: ParcelActionType,
    onActionTypeChange: (ParcelActionType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
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
                    selected = actionType == ParcelActionType.DISPATCH,
                    onClick = { onActionTypeChange(ParcelActionType.DISPATCH) }
                )
                Text(
                    text = ParcelActionType.DISPATCH.label,
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
                    selected = actionType == ParcelActionType.RECEIVE,
                    onClick = { onActionTypeChange(ParcelActionType.RECEIVE) }
                )
                Text(
                    text = ParcelActionType.RECEIVE.label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

enum class ParcelActionType(val label: String) {
    DISPATCH("Dispatch Parcels"),
    RECEIVE("Receive Parcels")
}