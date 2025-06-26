package com.buupass.quickshuttle.ui.screens.passengerbooking.confirm

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotInterested
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.buupass.quickshuttle.R
import com.buupass.quickshuttle.data.models.booking.CustomerDetails
import com.buupass.quickshuttle.navigation.AppScreens
import com.buupass.quickshuttle.ui.screens.passengerbooking.booking.BookingScreenViewModel
import com.buupass.quickshuttle.ui.screens.common.CustomAppBar
import com.buupass.quickshuttle.ui.screens.passengerbooking.payment.PaymentsViewModel
import com.buupass.quickshuttle.utils.generateValidPhoneNumber

@Composable
fun ConfirmBookingScreen(
    navController: NavController,
    bookingScreenViewModel: BookingScreenViewModel,
    paymentsViewModel: PaymentsViewModel
) {
    val bookingScreenUIState by bookingScreenViewModel.uiState.collectAsStateWithLifecycle()
    val passengerList = bookingScreenUIState.passengerList ?: emptyList()
    val bookingRequest = bookingScreenUIState.bookingRequest

    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Confirm Booking",
                navigationIcon = R.drawable.arrow_back_ic,
                actionIcon = if (passengerList.isNotEmpty()) R.drawable.check_ic else null,
                onActionIconClick = {
                    // get booking request from booking screen view model
                    bookingRequest?.let { request ->
                        paymentsViewModel.initializeBookingRequest(request)
                    }
                    // navigate to payment screen
                    navController.navigate(AppScreens.PaymentScreen.route)
                },
                onNavigationIconClick = { navController.popBackStack() }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    passengerList.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center

                        ) {
                            Icon(
                                modifier = Modifier.size(36.dp),
                                imageVector = Icons.Outlined.NotInterested,
                                contentDescription = "No passengers",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "No passengers",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    else -> {
                        LazyColumn {
                            items(items = passengerList) { customerDetails ->
                                PassengerDetailsCard(customerDetails = customerDetails)
                            }
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }

    )
}

@Composable
fun PassengerDetailsCard(
    customerDetails: CustomerDetails
) {
    // Create a list of pairs for attributes and their values from CustomerDetails
    val passengerDetails = listOf(
        "Name" to customerDetails.customerName,
        "Phone" to customerDetails.customerPhone.generateValidPhoneNumber(),
        "Pick Up" to customerDetails.pickupPoint,
        "Drop Off" to customerDetails.dropOffPoint,
        "Seat" to customerDetails.seatNumberSelected,
        "Fare" to customerDetails.amountToPay,
        "Date" to customerDetails.dateOfTravel
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, start = 8.dp, end = 8.dp)
            .wrapContentHeight(),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Iterate over the attributes and display them
            passengerDetails.forEach { (detailName, value) ->
                Row(
                    modifier = Modifier
                        .padding(vertical = 10.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = detailName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.weight(0.5f)
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}