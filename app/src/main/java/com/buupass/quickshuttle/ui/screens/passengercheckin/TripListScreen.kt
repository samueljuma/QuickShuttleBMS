package com.buupass.quickshuttle.ui.screens.passengercheckin

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.DirectionsBusFilled
import androidx.compose.material.icons.outlined.SyncAlt
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.buupass.quickshuttle.R
import com.buupass.quickshuttle.data.models.onboardingpassenger.ManifestTrip
import com.buupass.quickshuttle.navigation.AppScreens
import com.buupass.quickshuttle.ui.screens.common.CustomAppBar
import com.buupass.quickshuttle.ui.screens.common.LoadingDialog
import com.buupass.quickshuttle.ui.screens.parcelbooking.showparcels.EmptyItemsContainer
import com.buupass.quickshuttle.ui.screens.parcelbooking.showparcels.ErrorContainer
import com.buupass.quickshuttle.utils.formattedWithDayOfgWeek
import com.buupass.quickshuttle.utils.todayDate


@Composable
fun TripListScreen(
    navController: NavController,
    passengerCheckInViewModel: PassengerCheckInViewModel
) {

    val passengerCheckInUiState by passengerCheckInViewModel.uiState.collectAsStateWithLifecycle()
    val trips = passengerCheckInUiState.tripList
    val context = LocalContext.current

    BackHandler {
        passengerCheckInViewModel.clearTrips()
        navController.navigateUp()
    }

    LoadingDialog(
        isLoading = passengerCheckInUiState.isLoading,
        message = passengerCheckInUiState.loadingMessage
    )
    LaunchedEffect(Unit) {
        if (trips == null) {
            passengerCheckInViewModel.fetchTrips()
        }
    }

    LaunchedEffect(Unit) {
        passengerCheckInViewModel.passengerCheckInEvent.collect { event ->
            when (event) {

                is PassengerCheckInEvent.ShowErrorMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                is PassengerCheckInEvent.ShowSuccessMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Select Trip",
                navigationIcon = R.drawable.arrow_back_ic,
                actionIcon = R.drawable.close_ic,
                onNavigationIconClick = {
                    passengerCheckInViewModel.clearTrips()
                    navController.navigateUp()
                },
                onActionIconClick = {
                    passengerCheckInViewModel.clearTrips()
                    navController.navigateUp()

                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                when {
                    trips != null -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (trips.isEmpty()) {
                                item {
                                    EmptyItemsContainer(
                                        message = "No trips available"
                                    )
                                }
                            } else {
                                items(trips) { trip ->
                                    TripCard(
                                        trip = trip,
                                        onTripClick = {
                                            passengerCheckInViewModel.updateSelectedTrip(trip)
                                            navController.navigate(AppScreens.PassengerListScreen.route)
                                        }
                                    )
                                }
                            }
                            item {
                                Spacer(modifier = Modifier.height(60.dp))
                            }
                        }
                    }

                    passengerCheckInUiState.error != null -> {
                        ErrorContainer(
                            error = passengerCheckInUiState.error ?: "There was error fetching Trips"
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun TripCard(
    trip: ManifestTrip,
    onTripClick: (ManifestTrip) -> Unit,
    isForPassengerList: Boolean = false
) {

    val verticalTextPadding = if (isForPassengerList) 10.dp else 0.dp
    Column(
        modifier = Modifier
            .padding(horizontal = 5.dp, vertical = 2.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                enabled = !isForPassengerList,
                onClick = { onTripClick(trip) }
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Trip Name Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 10.dp,
                    start = 10.dp,
                    end = 10.dp,
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = trip.trip_name.split("-")[0]
            )
            Icon(
                imageVector = Icons.Outlined.SyncAlt,
                contentDescription = "Sync",
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = trip.trip_name.split("-")[1]
            )
            Spacer(modifier = Modifier.width(10.dp))
        }

        // Date and Forward Arrow Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = todayDate.formattedWithDayOfgWeek(),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(vertical = verticalTextPadding)
            )
            if (!isForPassengerList) {
                IconButton(
                    onClick = { onTripClick(trip) }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                        contentDescription = "Sync",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

        }

        // Departure Time and Bus Type Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            SuggestionChip(
                label = {
                    Text(
                        text = "Dep ${trip.schedule_starting_time}",
                        fontWeight = FontWeight.Bold
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = "Watch Icon",
                        tint = colorResource(R.color.green)
                    )
                },
                enabled = false,
                onClick = { /*TODO*/ },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurface
                )
            )
            SuggestionChip(
                modifier = Modifier.padding(end = if (isForPassengerList) 20.dp else 50.dp),
                label = {

                    Text(
                        text = trip.bus_type
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.DirectionsBusFilled,
                        contentDescription = "Seat Icon",
                        tint = MaterialTheme.colorScheme.primary.copy(0.8f)
                    )
                },
                enabled = false,
                onClick = { /*TODO*/ },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
        Spacer(modifier = Modifier.height(10.dp))


    }
}

