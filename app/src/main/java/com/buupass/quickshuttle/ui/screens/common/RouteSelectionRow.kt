package com.buupass.quickshuttle.ui.screens.common

import com.buupass.quickshuttle.data.models.parcel.ParcelRoute
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

@Composable
fun RouteSelectionRow(
    parcelRoute: ParcelRoute?,
    parcelRoutes: List<ParcelRoute>?,
    onParcelRouteSelected: (ParcelRoute) -> Unit,
) {
    var showRouteSelectionDialog by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "ROUTE",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.5f)
                .padding(start = 16.dp)
        )
        Button(
            onClick = { showRouteSelectionDialog = true },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text(
                text = parcelRoute?.name ?: "Select Route",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }

    // Show City To Selection Dialog
    if (showRouteSelectionDialog) {
        parcelRoutes?.let { parcelRoutes ->
            RouteSelectionDialogue(
                parcelRoutes = parcelRoutes,
                onParcelRouteSelected = { route ->
                    onParcelRouteSelected(route)
                    showRouteSelectionDialog = false
                },
                onDismiss = {
                    showRouteSelectionDialog = false
                }
            )
        }
    }
}