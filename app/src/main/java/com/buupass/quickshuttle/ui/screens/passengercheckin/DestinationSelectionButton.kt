package com.buupass.quickshuttle.ui.screens.passengercheckin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import com.buupass.quickshuttle.data.models.City
import com.buupass.quickshuttle.ui.screens.common.CitySelectionDialog


@Composable
fun DestinationSelectionButton(
    modifier: Modifier,
    city: City,
    cityList: List<City>?,
    onCitySelected: (City) -> Unit,
) {

    var showCitySelectionDialog by remember { mutableStateOf(false) }

    Button(
        onClick = { showCitySelectionDialog = true },
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Ensure the content fills max width and aligns left
        Box(
            modifier = Modifier.fillMaxWidth()
                .padding(start = 16.dp),
            contentAlignment = Alignment.CenterStart // Aligns content to start
        ) {
            Text(
                text = city.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }

    // Show City To Selection Dialog
    if (showCitySelectionDialog) {
        cityList?.let {
            CitySelectionDialog(
                cities = it,
                onCitySelected = { selectedCity ->
                    onCitySelected(selectedCity)

//                    bookingScreenViewModel.updateCityFromOrTo(selectedCity, isPickUp)
                    showCitySelectionDialog = false
                },
                onDismiss = {
                    showCitySelectionDialog = false
                }
            )
        }
    }
}