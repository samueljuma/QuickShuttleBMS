package com.buupass.quickshuttle.ui.screens.passengerbooking.booking

import com.buupass.quickshuttle.data.models.City
import com.buupass.quickshuttle.data.models.booking.CustomerDetails
import com.buupass.quickshuttle.data.models.booking.Schedule
import com.buupass.quickshuttle.data.models.payment.InitiateBookingRequest
import com.buupass.quickshuttle.domain.booking.SeatDomain
import com.buupass.quickshuttle.domain.booking.SeatLayout

// for tests
val fromCity = City(4, "Nairobi")
val toCity = City(5, "Nakuru")

val fromCity1 = City(8, "Madrid")
val toCity1 = City(9, "z")

data class BookingScreenUIState(
    val isLoading: Boolean = false,
    val loadingMessage: String = "",
    val errorMessage: String? = null,
    val departureDate: String = "SET DATE",
    val cityFrom: City = fromCity1,
    val cityTo: City = toCity1,
    val cityList: List<City>? = null,
    val seatLayout: SeatLayout? = null,
    val seatsToReserve: List<SeatDomain>? = null,
    val seatsToBook: List<SeatDomain>? = null,
    val schedules: List<Schedule>? = null,
    val selectedSchedule: Schedule? = null,
    val selectedSeat: SeatDomain? = null,
    val cachedCustomerDetails: CustomerDetails? = null,
    val freshCustomerDetails: CustomerDetails = CustomerDetails(),
    val passengerList: List<CustomerDetails>? = null,
    val showDatePickerForDepartureDate: Boolean = false,
    val showDatePickerForGettingBookings: Boolean = false,
    val showSchedulesDialog: Boolean = false,
    val dateForGettingPastBookings: String? = null,
    val navigateToShowBookingsScreen: Boolean = false,
    val bookingRequest: InitiateBookingRequest? = null,
    val showErrorMessageDialog: Boolean = false,
    val showSuccessMessageDialog: Boolean = false,
    val successMessage: String = "",
    val bookingFlowCompleted: Boolean = false,
    val showDatePickerForShowParcels: Boolean = false,
    val dateForShowParcels: String? = null
)