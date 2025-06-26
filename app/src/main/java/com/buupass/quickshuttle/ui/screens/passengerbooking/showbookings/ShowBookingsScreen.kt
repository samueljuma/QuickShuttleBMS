package com.buupass.quickshuttle.ui.screens.passengerbooking.showbookings

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.buupass.quickshuttle.R
import com.buupass.quickshuttle.data.models.booking.Booking
import com.buupass.quickshuttle.data.models.booking.Passenger
import com.buupass.quickshuttle.ui.screens.auth.AuthViewModel
import com.buupass.quickshuttle.ui.screens.common.CustomAppBar
import com.buupass.quickshuttle.ui.screens.common.LoadingDialog
import com.buupass.quickshuttle.ui.screens.common.MessageDialog
import com.buupass.quickshuttle.utils.capitalizeFirstCharacter
import com.buupass.quickshuttle.utils.formatAmount

@Composable
fun ShowBookingsScreen(
    navController: NavController,
    date: String?,
    showBookingsScreenViewModel: ShowBookingsScreenViewModel,
    authViewModel: AuthViewModel
) {
    val showBookingsUIState by showBookingsScreenViewModel.uiState.collectAsStateWithLifecycle()

    val showErrorMessageDialog = showBookingsUIState.showErrorMessageDialog
    val isLoading = showBookingsUIState.isLoading


    LaunchedEffect(Unit) {
        // fetch past bookings

        //Log Date
        Log.d("ShowBookingsScreen", "Date: $date")

        date?.let {
            showBookingsScreenViewModel.fetchPastBookings(
                date = it,
                userId = authViewModel.getUserDetails().id ?: 0
            )
        }
    }

    LoadingDialog(
        isLoading = isLoading,
        onDismiss = {},
        message = "Fetching bookings..."
    )

    when {
        showErrorMessageDialog -> {
            MessageDialog(
                onDismiss = {
                    showBookingsScreenViewModel.resetShowErrorMessageDialog()
                },
                dialogTitle = "Oops! Error!",
                dialogText = showBookingsUIState.errorMessage ?: "An error occurred",
                icon = Icons.Outlined.ErrorOutline,
                isErrorMessage = true
            )
        }
    }

    Scaffold(
        topBar = {
            CustomAppBar(
                title = "My Bookings",
                navigationIcon = R.drawable.arrow_back_ic,
                actionIcon = R.drawable.close_ic,
                onActionIconClick = { navController.popBackStack() },
                onNavigationIconClick = { navController.popBackStack() }
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
                when {
                    showBookingsUIState.errorMessage != null -> {
                        Icon(
                            imageVector = Icons.Outlined.ErrorOutline,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "There was an error when fetching bookings",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    showBookingsUIState.isLoading -> {
                        CircularProgressIndicator()
                    }
                    else -> {
                        BookingsTable(
                            pastBookings = showBookingsUIState.pastBookings,
                            date = date ?: "the selected date"
                        )
                    }
                }
            }
        }

    )
}

@Composable
fun BookingsTable(
    pastBookings: List<Booking>?,
    date: String
) {
    val hasNoPastBookings = pastBookings.isNullOrEmpty()

    if (hasNoPastBookings) {
        // Show blank state if both are empty or null
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "Info",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "You have no bookings for",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = date,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize()
            .padding(16.dp)
        ) {
            Text(
                text = "Bookings",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.secondary)
            BookingHeaderRow()
            HorizontalDivider(color = MaterialTheme.colorScheme.secondary)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
            ) {

                pastBookings?.let { bookings ->
                    //Get all passengers from all bookings
                    val passengerList = bookings.flatMap { it.passengers }

                    itemsIndexed(passengerList) { index, passenger ->
                        BookingDataRow(index = index, passenger = passenger)
                    }
                }
            }
            pastBookings?.let { pastBookings ->
                TotalsRow(bookings = pastBookings)
            }

        }
    }
}

@Composable
fun BookingHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        RecordText(text = "No.  ", modifier = Modifier.weight(0.7f), fontWeight = FontWeight.Bold)
        RecordText(text = "Name", modifier = Modifier.weight(2f), textAlign = TextAlign.Start, fontWeight = FontWeight.Bold)
        RecordText(text = "Phone Number", modifier = Modifier.weight(2.2f).padding(start = 4.dp), textAlign = TextAlign.Start, fontWeight = FontWeight.Bold)
        RecordText(text = "Seat", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        RecordText(text = "Fare", modifier = Modifier.weight(1f), textAlign = TextAlign.Start, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BookingDataRow(index: Int, passenger: Passenger) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        if (index > 0) {
            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(0.2f))
        }
        Row(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RecordText(text = "${index + 1}.", modifier = Modifier.weight(0.7f))
            RecordText(text = passenger.name.capitalizeFirstCharacter(), modifier = Modifier.weight(2f))
            RecordText(
                text = passenger.phone_number,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .weight(2f)
                    .padding(start = 4.dp),
                fontSize = 11.sp,
                textAlign = TextAlign.Start
            )
            RecordText(
                text = passenger.seat_number,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1.5f),
                textAlign = TextAlign.Center
            )
            RecordText(
                text = passenger.seat_price.formatAmount(),
                modifier = Modifier
                    .weight(1f),
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
fun RecordText(
    modifier: Modifier = Modifier,
    text: String,
    textAlign: TextAlign? = TextAlign.Start,
    fontSize: TextUnit = MaterialTheme.typography.bodySmall.fontSize,
    fontWeight: FontWeight = FontWeight.SemiBold,
    color: Color = MaterialTheme.colorScheme.onBackground,
    style: TextStyle = MaterialTheme.typography.bodySmall
) {
    Text(
        text = text,
        modifier = modifier.padding(vertical = 2.dp),
        textAlign = textAlign,
        color = color,
        style = style.copy(
            fontSize = fontSize,
            fontWeight = fontWeight
        )
    )
}

@Composable
fun TotalsRow(bookings: List<Booking>) {
    val totalAmount = bookings.sumOf { it.total_amount }
    val totalKode = bookings.sumOf { it.passengers.sumOf { it.kode } }
    val netAmount = totalAmount - totalKode

    val totals = listOf(
        "Total" to totalAmount,
        "Total Kode" to totalKode,
        "Net Amount" to netAmount
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, end = 8.dp)
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.secondary)

        totals.forEach { (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1.5f))
                    Text(
                        modifier = Modifier.weight(1f),
                        text = label,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.End
                    )
                    Text(
                        modifier = Modifier.padding(end = 8.dp)
                            .weight(1f),
                        text = value.formatAmount(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.secondary)
    }
}