package com.buupass.quickshuttle.navigation

sealed class AppScreens(val route: String) {
    object SplashScreen : AppScreens("splash_screen")
    object LoginScreen : AppScreens("login_screen")
    object BookingScreen : AppScreens("booking_screen")
    object ConfirmBookingScreen : AppScreens("confirm_booking_screen")
    object ShowBookingsScreen : AppScreens("show_bookings_screen/{date}"){
        fun createRoute(date: String) = "show_bookings_screen/$date"
    }
    object ReprintTicketScreen : AppScreens("reprint_ticket_screen")
    object ManualConfirmScreen : AppScreens("manual_confirm_screen")
    object PaymentScreen : AppScreens("payment_screen")
    object BookParcelScreen: AppScreens("book_parcel_screen")
    object DispatchReceiveParcelScreen: AppScreens("dispatch_receive_parcel_screen")
    object ShowParcelsScreen: AppScreens("show_parcels_screen/{date}"){
        fun createRoute(date: String) = "show_parcels_screen/$date"
    }
    object PassengerListScreen: AppScreens("passenger_list_screen")
    object PassengerCheckInScreen: AppScreens("passenger_check_in_screen/{cityList}"){
        fun createRoute(cityList: String) = "passenger_check_in_screen/$cityList"
    }
    object TripListScreen: AppScreens("trip_list_screen")
}