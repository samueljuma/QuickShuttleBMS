package com.buupass.quickshuttle.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.buupass.quickshuttle.MainViewModel
import com.buupass.quickshuttle.ui.screens.auth.AuthViewModel
import com.buupass.quickshuttle.ui.screens.auth.LoginScreen
import com.buupass.quickshuttle.ui.screens.passengerbooking.booking.BookingScreen
import com.buupass.quickshuttle.ui.screens.passengerbooking.booking.BookingScreenViewModel
import com.buupass.quickshuttle.ui.screens.common.printer.PrinterViewModel
import com.buupass.quickshuttle.ui.screens.parcelbooking.bookparcel.BookParcelScreen
import com.buupass.quickshuttle.ui.screens.parcelbooking.dispatchparcel.DispatchReceiveParcelScreen
import com.buupass.quickshuttle.ui.screens.parcelbooking.showparcels.ShowParcelsScreen
import com.buupass.quickshuttle.ui.screens.passengerbooking.confirm.ConfirmBookingScreen
import com.buupass.quickshuttle.ui.screens.passengerbooking.manualconfirm.ManualConfirmScreen
import com.buupass.quickshuttle.ui.screens.passengerbooking.payment.PaymentScreen
import com.buupass.quickshuttle.ui.screens.passengerbooking.payment.PaymentsViewModel
import com.buupass.quickshuttle.ui.screens.reprintticket.ReprintTicketScreen
import com.buupass.quickshuttle.ui.screens.reprintticket.ReprintTicketViewModel
import com.buupass.quickshuttle.ui.screens.passengerbooking.showbookings.ShowBookingsScreen
import com.buupass.quickshuttle.ui.screens.passengercheckin.PassengerCheckInScreen
import com.buupass.quickshuttle.ui.screens.passengercheckin.PassengerCheckInViewModel
import com.buupass.quickshuttle.ui.screens.passengercheckin.PassengerListScreen
import com.buupass.quickshuttle.ui.screens.passengercheckin.TripListScreen
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation(
    modifier: Modifier,
    mainViewModel: MainViewModel
) {
    val bookingScreenViewModel: BookingScreenViewModel = koinViewModel()
    val reprintTicketViewModel: ReprintTicketViewModel = koinViewModel()
    val authViewModel: AuthViewModel = koinViewModel()
    val printerViewModel: PrinterViewModel = koinViewModel()
    val paymentViewModel: PaymentsViewModel = koinViewModel()
    val passengerCheckInViewModel: PassengerCheckInViewModel = koinViewModel()


    val authToken = authViewModel.getAuthToken()

    val navController = rememberNavController()

    val startDestination = if (authToken.isNotEmpty()) AppScreens.BookingScreen.route else AppScreens.LoginScreen.route

    NavHost(navController = navController, startDestination = startDestination) {
        composable(AppScreens.LoginScreen.route) {
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        composable(AppScreens.BookingScreen.route) {
            BookingScreen(
                navController = navController,
                bookingScreenViewModel = bookingScreenViewModel,
                authViewModel = authViewModel,
                mainViewModel = mainViewModel,
            )
        }
        composable(AppScreens.ConfirmBookingScreen.route) {
            ConfirmBookingScreen(
                navController = navController,
                bookingScreenViewModel = bookingScreenViewModel,
                paymentsViewModel = paymentViewModel
            )
        }
        composable(AppScreens.PaymentScreen.route) {
            PaymentScreen(
                navController = navController,
                printerViewModel = printerViewModel,
                paymentsViewModel = paymentViewModel,
                bookingScreenViewModel = bookingScreenViewModel
            )
        }
        composable(AppScreens.ReprintTicketScreen.route) {
            ReprintTicketScreen(
                navController = navController,
                reprintTicketViewModel = reprintTicketViewModel,
                printerViewModel = printerViewModel
            )
        }
        composable(
            AppScreens.ShowBookingsScreen.route,
            arguments = listOf(navArgument("date") { type = NavType.StringType })
        ) { backStackEntry ->
            ShowBookingsScreen(
                navController = navController,
                date = backStackEntry.arguments?.getString("date") ?: "",
                showBookingsScreenViewModel = koinViewModel(),
                authViewModel = authViewModel
            )
        }
        composable(AppScreens.ManualConfirmScreen.route) {
            ManualConfirmScreen(
                navController = navController
            )
        }
        composable(AppScreens.BookParcelScreen.route) {
            BookParcelScreen(
                navController = navController,
                bookParcelViewModel = koinViewModel(),
                printerViewModel = printerViewModel
            )
        }
        composable(AppScreens.DispatchReceiveParcelScreen.route) {
            DispatchReceiveParcelScreen(
                navController = navController,
                dispatchReceiveParcelViewModel = koinViewModel()
            )
        }
        composable(
            AppScreens.ShowParcelsScreen.route,
            arguments = listOf(navArgument("date") { type = NavType.StringType })
        ) { backStackEntry ->
            ShowParcelsScreen(
                date = backStackEntry.arguments?.getString("date") ?: "",
                navController = navController,
                showParcelsViewModel = koinViewModel()
            )
        }

        composable(AppScreens.PassengerListScreen.route) {
            PassengerListScreen(
                navController = navController,
                passengerCheckInViewModel = passengerCheckInViewModel
            )
        }
        composable(
            AppScreens.PassengerCheckInScreen.route,
            arguments = listOf(navArgument("cityList") { type = NavType.StringType })
        ) { backStackEntry ->
            PassengerCheckInScreen(
                cityList = Json.decodeFromString(backStackEntry.arguments?.getString("cityList") ?: ""),
                navController = navController,
                passengerCheckInViewModel = passengerCheckInViewModel
            )
        }
        composable(AppScreens.TripListScreen.route) {
            TripListScreen(
                navController = navController,
                passengerCheckInViewModel = passengerCheckInViewModel
            )
        }
    }
}