package com.buupass.quickshuttle.ui.screens.passengerbooking.booking

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.buupass.quickshuttle.MainViewModel
import com.buupass.quickshuttle.R
import com.buupass.quickshuttle.domain.auth.UserDomain
import com.buupass.quickshuttle.domain.booking.SeatDomain
import com.buupass.quickshuttle.domain.booking.SeatLayout
import com.buupass.quickshuttle.navigation.AppScreens
import com.buupass.quickshuttle.ui.screens.common.CitySelectionRow
import com.buupass.quickshuttle.ui.screens.common.CustomAppBar
import com.buupass.quickshuttle.ui.screens.common.CustomButton
import com.buupass.quickshuttle.ui.screens.common.LoadingDialog
import com.buupass.quickshuttle.ui.screens.common.MessageDialog
import com.buupass.quickshuttle.utils.formatted
import com.buupass.quickshuttle.utils.getSeatColor
import com.buupass.quickshuttle.utils.showDatePickerDialog

@Composable
fun BookingScreenContent(
    onNavigationIconClick: () -> Unit,
    bookingScreenViewModel: BookingScreenViewModel,
    mainViewModel: MainViewModel,
    navController: NavController,
    currentUser: UserDomain
) {
    val context = LocalContext.current

    val isConnected by mainViewModel.isOnline.collectAsStateWithLifecycle()

    val bookingScreenUIState by bookingScreenViewModel.uiState.collectAsStateWithLifecycle()

    var showCustomerDetailsDialog by remember { mutableStateOf(false) }
    val showErrorMessageDialog = bookingScreenUIState.showErrorMessageDialog

    val cityFrom = bookingScreenUIState.cityFrom
    val cityTo = bookingScreenUIState.cityTo
    val departureDate = bookingScreenUIState.departureDate
    val seatLayout = bookingScreenUIState.seatLayout
    val schedules = bookingScreenUIState.schedules ?: emptyList()
    val showDatePickerForDeparture = bookingScreenUIState.showDatePickerForDepartureDate
    val showDatePickerForGettingBookings = bookingScreenUIState.showDatePickerForGettingBookings
    val showSchedulesDialog = bookingScreenUIState.showSchedulesDialog
    val navigateToShowBookingsScreen = bookingScreenUIState.navigateToShowBookingsScreen
    val cachedCustomerDetails = bookingScreenUIState.cachedCustomerDetails
    val customerDetailsIsValid = bookingScreenUIState.freshCustomerDetails.isValid
    val selectedSeat = bookingScreenUIState.selectedSeat
    val showDatePickerForShowParcels = bookingScreenUIState.showDatePickerForShowParcels

    val showSuccessMessageDialog = bookingScreenUIState.showSuccessMessageDialog

    LaunchedEffect(Unit) {
        bookingScreenViewModel.bookingScreenEvent.collect { event ->
            when(event){
                is BookingScreenEvent.NavigateToShowBookingScreen -> {
                    navController.navigate(AppScreens.ShowBookingsScreen.createRoute(event.date))
                }
                is BookingScreenEvent.NavigateToShowParcels -> {
                    navController.navigate(AppScreens.ShowParcelsScreen.createRoute(event.date))
                }
            }
        }
    }

    LaunchedEffect(isConnected) {
        if (isConnected) {
            if (bookingScreenUIState.cityList == null) {
            bookingScreenViewModel.getCities()
            }
        }
    }

    when {
        showDatePickerForDeparture -> {
            // Show Date Picker if needed
            showDatePickerDialog(
                context,
                onDateSelected = { selectedDate ->
                    bookingScreenViewModel.updateDepartureDate(selectedDate.formatted())
                    bookingScreenViewModel.updateShowDatePickerForDepartureDate(false)
                    Log.d("BookingScreen", "Date selected: ${selectedDate.formatted()}")
                    bookingScreenViewModel.getSchedules()
                },
                onDismiss = {
                    bookingScreenViewModel.updateShowDatePickerForDepartureDate(false)
                }
            )
        }

        showDatePickerForGettingBookings -> {
            showDatePickerDialog(
                context,
                onDateSelected = { selectedDate ->
                    bookingScreenViewModel.updateDateForGettingPastBookings(
                        selectedDate.formatted()
                    )
                    bookingScreenViewModel.resetSelectedSchedule() // Reset Ui state to have no schedule selected
                    bookingScreenViewModel.updateShowDatePickerForGettingBookings(false)
                    bookingScreenViewModel.triggerNavigationToShowBookings()
                },
                onDismiss = {
                    bookingScreenViewModel.updateShowDatePickerForGettingBookings(false)
                }
            )
        }
        showDatePickerForShowParcels -> {
            showDatePickerDialog(
                context,
                onDateSelected = { selectedDate ->
                    bookingScreenViewModel.updateDateForShowParcels(
                        selectedDate.formatted()
                    )
                    bookingScreenViewModel.resetSelectedSchedule() // Reset Ui state to have no schedule selected
                    bookingScreenViewModel.updateShowDatePickerForShowParcels(false)
                    bookingScreenViewModel.triggerNavigationToShowParcels()
                },
                onDismiss = {
                    bookingScreenViewModel.updateShowDatePickerForShowParcels(false)
                }
            )
        }


        showSchedulesDialog -> {
            ScheduleSelectionDialog(
                schedules = schedules,
                onScheduleSelected = { selectedSchedule ->
                    bookingScreenViewModel.updateSelectedSchedule(selectedSchedule)
                    bookingScreenViewModel.getSeatsAvailable()
                },
                onDismiss = {
                    // Close the dialog
                    bookingScreenViewModel.resetShowSchedulesDialog()
                }
            )
        }

        showCustomerDetailsDialog -> {
            CustomerDetailsDialog(
                onDoneClicked = {
                    selectedSeat?.let { seat ->
//                    bookingScreenViewModel.setSeatSelectedForPassenger(seat)
                        bookingScreenViewModel.validateAllFreshCustomerDetails(seat.seatPrice)
                    }
                    // check if all fields are valid
                    if (customerDetailsIsValid) {
                        // add customer details to list of booking customers
                        bookingScreenViewModel.updatePassengerList(
                            bookingScreenUIState.freshCustomerDetails
                        )

                        // save customer details for autofill
                        bookingScreenViewModel.upDateCachedCustomerDetails(
                            bookingScreenUIState.freshCustomerDetails
                        )
                        // add seat to book list - important for color coding
                        selectedSeat?.let {
                            bookingScreenViewModel.addSeatToBookList(it)
                        }

                        showCustomerDetailsDialog = false
                    } else {
                        Toast.makeText(
                            context,
                            "Ensure you have filled all fields correctly",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onCancelClicked = {
                    bookingScreenViewModel.resetFreshCustomerDetails()
                    showCustomerDetailsDialog = false
                },
                onAutoFillClicked = {
                    if (cachedCustomerDetails != null) {
                        bookingScreenViewModel.autoFillFreshCustomerDetails(cachedCustomerDetails)

                        selectedSeat?.let {
                            bookingScreenViewModel.validateAllFreshCustomerDetails(it.seatPrice)
                        }
                    } else {
                        Toast.makeText(context, "No Existing Customer Details", Toast.LENGTH_SHORT).show()
                    }
                },
                onDismiss = {
                    showCustomerDetailsDialog = false
                },
                bookingScreenViewModel = bookingScreenViewModel,
                currency = currentUser.currency
            )
        }

        showErrorMessageDialog -> {
            MessageDialog(
                onDismiss = {
                    bookingScreenViewModel.resetShowErrorMessageDialog()
                },
                dialogTitle = "Oops! Error!",
                dialogText = bookingScreenUIState.errorMessage ?: "Something went Wrong",
                icon = Icons.Outlined.ErrorOutline,
                isErrorMessage = true
            )
        }

        showSuccessMessageDialog -> {
            MessageDialog(
                onDismiss = {
                    bookingScreenViewModel.resetShowSuccessMessageDialog()
                },
                dialogTitle = "Success!",
                dialogText = bookingScreenUIState.successMessage,
                icon = Icons.Outlined.Info
            )
        }
    }
    LaunchedEffect(cityFrom, cityTo, departureDate) {
        // clear schedules, selected schedule, seats layout
        bookingScreenViewModel.resetSeatLayoutAndSchedules()
    }

    LaunchedEffect(Unit) {
        if (bookingScreenUIState.cityList == null) {
            bookingScreenViewModel.getCities()
        }
    }

    LaunchedEffect(navigateToShowBookingsScreen) {
        if (navigateToShowBookingsScreen) {
            navController.navigate(AppScreens.ShowBookingsScreen.route)
        }
    }

    LaunchedEffect(Unit) {
        // if trip schedule is not null re-fetch seats available
        if (bookingScreenUIState.selectedSchedule != null) {
            // reset Ui State to only have required states
            bookingScreenViewModel.resetUiState()

            // fetch seats available
            bookingScreenViewModel.getSeatsAvailable()
        }
    }

    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Bus Booking",
                navigationIcon = R.drawable.menu_ic,
                actionIconIsMoreVert = true,
                actionIcon = R.drawable.more_action_ic,
                menuItems = listOf(
                    "Delivery Note" to { Toast.makeText(context, "Coming Soon", Toast.LENGTH_SHORT).show() },
                    "Sign Out" to {
                        bookingScreenViewModel.logoutUser()
                        navController.navigate(AppScreens.LoginScreen.route){
                            popUpTo(AppScreens.BookingScreen.route){
                                inclusive = true
                            }
                        }
                    }
                ),
                onNavigationIconClick = { onNavigationIconClick() }
            )
        },
        content = { innerPadding ->

            LoadingDialog(
                isLoading = bookingScreenUIState.isLoading,
                message = bookingScreenUIState.loadingMessage,
            )
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CitySelectionRow(
                    city = cityFrom,
                    isPickUp = true,
                    cityList = bookingScreenUIState.cityList,
                    onCitySelected = { selectedCity ->
                        bookingScreenViewModel.updateCityFromOrTo(selectedCity, isPickUp = true)
                    }

                )
                CitySelectionRow(
                    city = cityTo,
                    isPickUp = false,
                    cityList = bookingScreenUIState.cityList,
                    onCitySelected = { selectedCity ->
                        bookingScreenViewModel.updateCityFromOrTo(selectedCity, isPickUp = false)
                    }
                )
                DateSelectionRow(
                    date = departureDate,
                    bookingScreenViewModel = bookingScreenViewModel
                )

                if (seatLayout == null) {
                    Spacer(modifier = Modifier.weight(1f))

                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "Seat Layout will Appear here",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    CustomButton(
                        text = "BOOK",
                        enabled = false,
                        onClick = { /**TODO*/ },
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        buttonColor = MaterialTheme.colorScheme.tertiary
                    )

                    CustomButton(
                        text = "RESERVE",
                        enabled = false,
                        onClick = { /**TODO*/ },
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        buttonColor = MaterialTheme.colorScheme.secondary
                    )
                } else {
                    Text(
                        text = "SELECT SEAT",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    SeatsLayoutSection(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        seatLayout = bookingScreenUIState.seatLayout!!,
                        onSeatClicked = { seat ->
                            // set show Customer Details Dialog to true
                            showCustomerDetailsDialog = true

                            // update selected seat
                            bookingScreenViewModel.updateSelectedSeat(seat)

                            bookingScreenViewModel.setSeatAsSelected(
                                seat,
                                SeatInteraction.PRESS
                            )
                        },
                        onSeatLongPressed = { seat ->
                            bookingScreenViewModel.setSeatAsSelected(
                                seat,
                                SeatInteraction.LONG_PRESS
                            )
                        },
                        onReserveButtonClicked = {
                            bookingScreenViewModel.reserveSelectedSeats()
                        },
                        onBookButtonClicked = {
                            bookingScreenViewModel.bookSelectedSeats()
                            if (!bookingScreenUIState.passengerList.isNullOrEmpty()) {
                                navController.navigate(AppScreens.ConfirmBookingScreen.route) // TODO use shared flow
                            }
                        },
                        currentUser = currentUser,
                        state = bookingScreenUIState
                    )
                }
            }
        }

    )
}


@Composable
fun DateSelectionRow(
    date: String,
    bookingScreenViewModel: BookingScreenViewModel
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "DEPARTURE DATE",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.8f)
        )
        Spacer(modifier = Modifier.weight(0.1f))
        Button(
            onClick = {
                bookingScreenViewModel.updateShowDatePickerForDepartureDate(true)
            },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun SeatsLayoutSection(
    modifier: Modifier,
    seatLayout: SeatLayout,
    onSeatClicked: (SeatDomain) -> Unit,
    onSeatLongPressed: (SeatDomain) -> Unit,
    onReserveButtonClicked: () -> Unit,
    onBookButtonClicked: () -> Unit,
    currentUser: UserDomain,
    state: BookingScreenUIState
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        LazyVerticalGrid(
            modifier = Modifier.padding(16.dp),
            columns = GridCells.Fixed(seatLayout.columns)
        ) {
            items(seatLayout.seats) { seat ->
                Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp, horizontal = 5.dp)
                        .size(40.dp)
                        .background(
                            color = seat.getSeatColor(currentUser),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable(
                            enabled = seat.isValidSeatInBusLayout() && seat.isAvailable(
                                currentUser.id
                            )
                        ) {
                            if (seat.isValidSeatInBusLayout()) {
                                onSeatClicked(seat)
                            }
                        }
                        .pointerInput(Unit) {
                            if (seat.isValidSeatInBusLayout() && seat.isAvailable(currentUser.id)) {
                                detectTapGestures(
                                    onTap = {
                                        //
                                        onSeatClicked(seat)
                                    },
                                    onLongPress = {
                                        onSeatLongPressed(seat)
                                    }
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = seat.seatNumber,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            item(span = { GridItemSpan(seatLayout.columns) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CustomButton(
                        text = "BOOK",
                        onClick = { onBookButtonClicked() },
                        modifier = Modifier
                            .padding(vertical = 8.dp),
                        buttonColor = MaterialTheme.colorScheme.tertiary
                    )

                    CustomButton(
                        text = "RESERVE",
                        onClick = { onReserveButtonClicked() },
                        modifier = Modifier
                            .padding(vertical = 8.dp),
                        buttonColor = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}