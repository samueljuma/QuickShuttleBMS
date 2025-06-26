package com.buupass.quickshuttle.ui.screens.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.buupass.quickshuttle.data.models.City

@Composable
fun CitySelectionRow(
    city: City,
    isPickUp: Boolean,
    cityList: List<City>?,
    onCitySelected: (City) -> Unit,
    isParcelPointsSelection : Boolean = false
) {

    val labelWeight = if (!isParcelPointsSelection) 0.8f else 0.5f
    val textAlignment = if (!isParcelPointsSelection) TextAlign.End else TextAlign.Start
    val labelPadding = if (!isParcelPointsSelection) 0.dp else 16.dp

    val labelText = when(isParcelPointsSelection){
        true -> if(isPickUp) "PICKUP" else "DROP OFF"
        false -> if(isPickUp) "FROM" else "TO"
    }

    var showCitySelectionDialog by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = labelText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            textAlign = textAlignment,
            modifier = Modifier.weight(labelWeight)
                .padding(start = labelPadding)
        )
        if(!isParcelPointsSelection){
            Spacer(modifier = Modifier.weight(0.1f))
        }
        Button(
            onClick = {
                //If cityList is not null, show the city selection dialog
                cityList?.let {
                    showCitySelectionDialog = true
                }
            },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text(
                text = city.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
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