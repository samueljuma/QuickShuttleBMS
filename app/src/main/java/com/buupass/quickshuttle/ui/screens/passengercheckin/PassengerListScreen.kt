package com.buupass.quickshuttle.ui.screens.passengercheckin

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.buupass.quickshuttle.R
import com.buupass.quickshuttle.data.models.onboardingpassenger.PassengerToOnboard
import com.buupass.quickshuttle.ui.screens.common.CustomAppBar
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.buupass.quickshuttle.ui.screens.common.LoadingDialog
import com.buupass.quickshuttle.ui.screens.parcelbooking.showparcels.EmptyItemsContainer
import com.buupass.quickshuttle.utils.capitalizeFirstCharacter
import com.buupass.quickshuttle.utils.seatNumberAlphaPart
import com.buupass.quickshuttle.utils.seatNumberNumericPart

@Composable
fun PassengerListScreen(
    navController: NavController,
    passengerCheckInViewModel: PassengerCheckInViewModel
) {

    var searchQuery by remember { mutableStateOf("") }
    var showPassengerOnboardingDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var selectedPassenger by remember { mutableStateOf<PassengerToOnboard?>(null) }

    val passengerCheckInUiState by passengerCheckInViewModel.uiState.collectAsStateWithLifecycle()

    val passengerList = passengerCheckInUiState.passengersList


    val filteredPassengerList by rememberUpdatedState(
        newValue = passengerList.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.seat_number.contains(searchQuery, ignoreCase = true)
        }.sortedWith(
            compareBy(
                { it.seatNumberNumericPart() }, // Sort numerically
                { it.seatNumberAlphaPart() }    // Then alphabetically
        ))
    )


    LaunchedEffect(Unit) {
        passengerCheckInViewModel.passengerCheckInEvent.collect{ event ->
            when(event){
                is PassengerCheckInEvent.ShowSuccessMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is PassengerCheckInEvent.ShowErrorMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    when{
        showPassengerOnboardingDialog -> {
            selectedPassenger?.let { passengerToOnboard ->
                PassengerOnboardingDialog(
                    onDismiss = { showPassengerOnboardingDialog = false },
                    passenger = passengerToOnboard,
                    onOnboardPassengerClicked = {
                        showPassengerOnboardingDialog = false
                        passengerCheckInViewModel.onboardPassenger(passengerToOnboard.passenger_id)
                    },
                    onCancelClicked = {
                        showPassengerOnboardingDialog = false
                    },
                    onPhoneIconClicked = { number ->
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = "tel:${number}".toUri()
                        }
                        context.startActivity(intent)
                    }

                )
            }
        }
    }

    BackHandler {
        passengerCheckInViewModel.clearPassengersToOnboard()
        navController.navigateUp()
    }
    LoadingDialog(
        isLoading = passengerCheckInUiState.isLoading,
        message = passengerCheckInUiState.loadingMessage
    )
    LaunchedEffect(Unit) {
        passengerCheckInViewModel.fetchPassengerList()
    }

    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Passenger List",
                navigationIcon = R.drawable.arrow_back_ic,
                actionIcon = R.drawable.close_ic,
                onNavigationIconClick = {
                    passengerCheckInViewModel.clearPassengersToOnboard()
                    navController.navigateUp()
                },
                onActionIconClick = {
                    passengerCheckInViewModel.clearPassengersToOnboard()
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
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ){
                    LeadingIconText(
                        icon = Icons.Filled.Chair,
                        text = "Onboarded",
                        iconTint = colorResource(R.color.green),
                        modifier = Modifier.padding()
                    )
                    LeadingIconText(
                        icon = Icons.Filled.Chair,
                        text = "Not onboarded",
                        iconTint = Color.LightGray.copy(0.7f),
                        modifier = Modifier.padding()
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                passengerCheckInUiState.selectedTrip?.let {
                    TripCard(
                        it,
                        onTripClick = {},
                        isForPassengerList = true
                    )
                }

//                Spacer(modifier = Modifier.height(8.dp))
//                OutlinedTextField(
//                    value = searchQuery,
//                    onValueChange = { searchQuery = it },
//                    placeholder = { Text("Search by Name or Seat...") },
//                    modifier = Modifier.fillMaxWidth()
//                        .padding(horizontal = 16.dp),
//                    singleLine = true,
//                    trailingIcon = {
//                        Icon(
//                            imageVector = Icons.Outlined.Search,
//                            contentDescription = "Search Icon"
//                        )
//                    }
//                )
                Spacer(modifier = Modifier.height(10.dp))

                when{
                    passengerList.isEmpty() && !passengerCheckInUiState.isLoading -> {
                        EmptyItemsContainer(
                            message = "No passengers"
                        )
                    }
                    else ->{
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(items = filteredPassengerList) { passenger->
                                PassengerCard(
                                    passenger = passenger,
                                    onPassengerClick = { mPassenger ->
                                        selectedPassenger = mPassenger
                                        showPassengerOnboardingDialog = true
                                    }
                                )
                            }
                            item{
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }

            }
        }
    )
}

@Composable
fun PassengerCard(
    passenger: PassengerToOnboard,
    onPassengerClick: (PassengerToOnboard) -> Unit
){
    val seatColor = if(passenger.onboarded)
        colorResource(R.color.green)
    else Color.LightGray.copy(0.7f)

    val seatNumberColor = if(passenger.onboarded)
        MaterialTheme.colorScheme.surface
    else MaterialTheme.colorScheme.tertiary

    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clickable {
                onPassengerClick(passenger)
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically
            ){
                Box(
                    modifier = Modifier.padding(end = 8.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary)
                ){
                    Text(
                        text = passenger.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.padding(end = 16.dp))
                Text(
                    text = passenger.name.capitalizeFirstCharacter(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Box(
                modifier = Modifier.padding(end = 20.dp),
                contentAlignment = Alignment.Center
            ){
                Icon(
                    imageVector = Icons.Filled.Chair,
                    contentDescription = "Seat Icon",
                    modifier = Modifier.size(40.dp),
                    tint = seatColor
                )
                Text(
                    text = passenger.seat_number,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = seatNumberColor,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    modifier = Modifier.padding(bottom = 14.dp)
                )
            }

        }
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            Spacer(modifier = Modifier.width(50.dp))
            HorizontalDivider(modifier = Modifier.padding(end = 8.dp))
        }
    }
}

@Composable
fun LeadingIconText(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text: String,
    contentDescription: String? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = textStyle
        )
    }
}
