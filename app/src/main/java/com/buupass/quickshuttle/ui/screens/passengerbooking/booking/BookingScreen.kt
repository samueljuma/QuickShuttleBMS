package com.buupass.quickshuttle.ui.screens.passengerbooking.booking

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.buupass.quickshuttle.MainViewModel
import com.buupass.quickshuttle.navigation.AppScreens
import com.buupass.quickshuttle.ui.screens.auth.AuthViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun BookingScreen(
    bookingScreenViewModel: BookingScreenViewModel,
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel,
    navController: NavController
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val bookingUiState by bookingScreenViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (bookingUiState.cityList == null) {
            bookingScreenViewModel.getCities()
        }
    }

    fun getCurrentUser() = authViewModel.getUserDetails()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            BookingScreenDrawerContent(
                user = getCurrentUser(),
                onItemSelected = { label ->

                    when (label) {
                        BookingsDrawerItemLabels.BOOK_PASSENGER.label -> {
                            // Handle Book Passenger
                        }
                        BookingsDrawerItemLabels.SHOW_BOOKINGS.label -> {
                            bookingScreenViewModel.updateShowDatePickerForGettingBookings(
                                showDatePicker = true
                            )
//                            bookingScreenViewModel.resetSelectedSchedule()
                        }
//                        BookingsDrawerItemLabels.ONBOARD_PASSENGER.label -> {
//                            navController.navigate(AppScreens.PassengerCheckInScreen.createRoute(
//                                Json.encodeToString(bookingUiState.cityList)
//                            ))
//                            bookingScreenViewModel.resetSelectedSchedule()
//                        }
                        BookingsDrawerItemLabels.CREATE_SCHEDULE.label -> {
                            //TODO
                        }
                        BookingsDrawerItemLabels.ASSIGN_TOKEN.label -> {
                            //TODO
                        }
                        BookingsDrawerItemLabels.SHOW_SCHEDULES.label -> {
                            //TODO
                        }

                        ParcelDrawerItemLabels.BOOK_PARCEL.label -> {
                            navController.navigate(AppScreens.BookParcelScreen.route)
                            // Reset selected schedule
                            bookingScreenViewModel.resetSelectedSchedule()
                        }
                        ParcelDrawerItemLabels.DISPATCH_OR_RECEIVE_PARCEL.label -> {
                            navController.navigate(AppScreens.DispatchReceiveParcelScreen.route)
                            // Reset selected schedule
                            bookingScreenViewModel.resetSelectedSchedule()
                        }
                        ParcelDrawerItemLabels.SHOW_PARCELS.label -> {
                            // Reset selected schedule
                            bookingScreenViewModel.resetSelectedSchedule()
                            bookingScreenViewModel.updateShowDatePickerForShowParcels(true)
                        }
                    }

                    coroutineScope.launch {
                        drawerState.close()
                    }
                }

            )
        }
    ) {
        BookingScreenContent(
            bookingScreenViewModel = bookingScreenViewModel,
            mainViewModel = mainViewModel,
            onNavigationIconClick = {
                coroutineScope.launch {
                    drawerState.open()
                }
            },
            navController = navController,
            currentUser = getCurrentUser()
        )
    }
}