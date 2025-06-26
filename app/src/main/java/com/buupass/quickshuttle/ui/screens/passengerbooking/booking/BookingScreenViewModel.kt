package com.buupass.quickshuttle.ui.screens.passengerbooking.booking

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buupass.quickshuttle.data.models.City
import com.buupass.quickshuttle.data.models.booking.CustomerDetails
import com.buupass.quickshuttle.data.models.booking.ReserveSeatsRequest
import com.buupass.quickshuttle.data.models.booking.Schedule
import com.buupass.quickshuttle.data.models.booking.toPassengerDetail
import com.buupass.quickshuttle.data.models.booking.toSeatLayout
import com.buupass.quickshuttle.data.models.payment.InitiateBookingRequest
import com.buupass.quickshuttle.data.network.NetworkResult
import com.buupass.quickshuttle.data.repositories.AuthRepository
import com.buupass.quickshuttle.data.repositories.BookingRepository
import com.buupass.quickshuttle.domain.booking.SeatDomain
import com.buupass.quickshuttle.utils.resolvePickupAndDropOffPoints
import com.buupass.quickshuttle.utils.validateAmount
import com.buupass.quickshuttle.utils.validateId
import com.buupass.quickshuttle.utils.validateName
import com.buupass.quickshuttle.utils.validateResidence
import com.buupass.quickshuttle.utils.validatePhone
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.get

class BookingScreenViewModel(
    private val authRepository: AuthRepository,
    private val bookingRepository: BookingRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(BookingScreenUIState())
    val uiState: StateFlow<BookingScreenUIState> = _uiState.asStateFlow()

    private val _navigateToShowBookingsScreen = MutableSharedFlow<Unit>()
    val navigateToShowBookingsScreen: SharedFlow<Unit> = _navigateToShowBookingsScreen

    private val _bookingScreenEvent = MutableSharedFlow<BookingScreenEvent>()
    val bookingScreenEvent: SharedFlow<BookingScreenEvent> = _bookingScreenEvent

    fun triggerNavigationToShowBookings() {
        viewModelScope.launch {
            _navigateToShowBookingsScreen.emit(Unit)
            val date = _uiState.value.dateForGettingPastBookings
            date?.let {
                _bookingScreenEvent.emit(
                    BookingScreenEvent.NavigateToShowBookingScreen(it)
                )
            }
        }
    }

    fun triggerNavigationToShowParcels() {
        viewModelScope.launch {
            val date = _uiState.value.dateForShowParcels
            date?.let {
                _bookingScreenEvent.emit(
                    BookingScreenEvent.NavigateToShowParcels(it)
                )
            }
        }
    }


    fun getCities() {
        _uiState.update {
            it.copy(
                isLoading = true,
                loadingMessage = "Fetching Cities"
            )
        }
        viewModelScope.launch {
            val result = bookingRepository.getCities()
            when (result) {
                is NetworkResult.Success -> {
                    val cities = result.data.cities
                    _uiState.update {
                        it.copy(
                            cityList = cities,
                            cityFrom = cities[0],
                            cityTo = cities[1],
                            isLoading = false,
                            loadingMessage = "",
                            errorMessage = null
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadingMessage = "",
                            errorMessage = result.message,
                            showErrorMessageDialog = true
                        )
                    }
                }
            }
        }
    }

    fun updateCityFromOrTo(city: City, isPickUp: Boolean) {
        _uiState.update {
            if (isPickUp) {
                it.copy(cityFrom = city)
            } else {
                it.copy(cityTo = city)
            }
        }
    }

    fun updateDepartureDate(date: String) {
        _uiState.update {
            it.copy(departureDate = date)
        }
    }

    fun resetSeatLayoutAndSchedules() {
        _uiState.update {
            it.copy(
                seatLayout = null,
                schedules = null
            )
        }
    }

    fun getSchedules() {
        _uiState.update {
            it.copy(
                isLoading = true,
                schedules = null,
                selectedSchedule = null,
                seatLayout = null,
                loadingMessage = "Fetching Schedules"
            )
        }
        viewModelScope.launch {
            val schedulesResult = bookingRepository.getSchedules(
                uiState.value.cityFrom.id,
                uiState.value.cityTo.id,
                uiState.value.departureDate
            )
            when (schedulesResult) {
                is NetworkResult.Success -> {
                    val schedules = schedulesResult.data.trip_list
                    _uiState.update {
                        it.copy(
                            schedules = schedules,
                            isLoading = false,
                            loadingMessage = "",
                            showSchedulesDialog = true
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadingMessage = "",
                            errorMessage = schedulesResult.message,
                            showErrorMessageDialog = true
                        )
                    }
                }
            }
        }
    }

    fun updateSelectedSchedule(schedule: Schedule) {
        _uiState.update { state ->
            val (pickupPoint, dropOffPoint) = resolvePickupAndDropOffPoints(
                schedule,
                state.cityFrom.name,
                state.cityTo.name
            )

            state.copy(
                selectedSchedule = schedule,
                freshCustomerDetails = state.freshCustomerDetails.copy(
                    pickupPoint = pickupPoint,
                    dropOffPoint = dropOffPoint
                ),
                showSchedulesDialog = false
            )
        }
    }


    fun resetShowSchedulesDialog() {
        _uiState.update {
            it.copy(
                showSchedulesDialog = false
            )
        }
    }

    fun resetShowErrorMessageDialog() {
        _uiState.update {
            it.copy(
                showErrorMessageDialog = false
            )
        }
    }

    fun getSeatsAvailable() {
        _uiState.update {
            it.copy(
                isLoading = true,
                seatLayout = null,
                loadingMessage = "Fetching Seats"
            )
        }
        viewModelScope.launch {
            val result = _uiState.value.selectedSchedule?.let { schedule ->
                bookingRepository.getAvailableSeats(
                    _uiState.value.cityFrom.id,
                    _uiState.value.cityTo.id,
                    _uiState.value.departureDate,
                    schedule.trip_schedule_id
                )
            }
            when (result) {
                is NetworkResult.Success -> {
                    val seatLayout = result.data.schedule.toSeatLayout()
                    Log.d("BookingScreenViewModel", "seatLayout: $seatLayout")
                    _uiState.update {
                        it.copy(
                            seatLayout = seatLayout,
                            isLoading = false,
                            loadingMessage = "",
                            errorMessage = null
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadingMessage = "",
                            errorMessage = result.message,
                            showErrorMessageDialog = true
                        )
                    }
                }
                null -> TODO()
            }
        }
    }

    // if is
    fun upDateCustomerPickupOrDropOff(isPickUp: Boolean, location: String) {
        _uiState.update {
            if (isPickUp) {
                it.copy(
                    freshCustomerDetails = it.freshCustomerDetails.copy(
                        pickupPoint = location
                    )
//                    seatLayout = null //TODO
                )
            } else {
                it.copy(
                    freshCustomerDetails = it.freshCustomerDetails.copy(
                        dropOffPoint = location
                    )
//                    seatLayout = null
                )
            }
        }
    }

    fun addSeatToBookList(seat: SeatDomain) {
        _uiState.update { state ->
            val currentSeatsToBook = state.seatsToBook ?: emptyList()

            val updatedSeats = state.seatLayout?.seats?.map { existingSeat ->
                if (existingSeat.seatNumber == seat.seatNumber) {
                    existingSeat.copy(isSelectedForBooking = true)
                } else {
                    existingSeat
                }
            } ?: emptyList()

            state.copy(
                seatLayout = state.seatLayout?.copy(seats = updatedSeats),
                seatsToBook = if (currentSeatsToBook.any { it.seatNumber == seat.seatNumber }) {
                    currentSeatsToBook
                } else {
                    currentSeatsToBook + seat
                }
            )
        }
    }

    fun updateShowDatePickerForDepartureDate(showDatePicker: Boolean) {
        _uiState.update {
            it.copy(showDatePickerForDepartureDate = showDatePicker)
        }
    }
    fun updateShowDatePickerForGettingBookings(showDatePicker: Boolean) {
        _uiState.update {
            it.copy(showDatePickerForGettingBookings = showDatePicker)
        }
    }

    fun updateDateForGettingPastBookings(date: String) {
        _uiState.update {
            it.copy(
                dateForGettingPastBookings = date
            )
        }
    }
    fun updateShowDatePickerForShowParcels(showDatePicker: Boolean) {
        _uiState.update {
            it.copy(showDatePickerForShowParcels = showDatePicker)
        }
    }

    fun updateDateForShowParcels(date: String) {
        _uiState.update {
            it.copy(
                dateForShowParcels = date
            )
        }
    }


    //
    fun updateSelectedSeat(seat: SeatDomain) {
        _uiState.update { state ->
            state.copy(
                selectedSeat = seat,
                freshCustomerDetails = state.freshCustomerDetails.copy(
                    seatNumberSelected = seat.seatNumber
                )
            )
        }
    }

    //
    fun setSeatAsSelected(seat: SeatDomain, interaction: SeatInteraction) {
        _uiState.update { uiState ->
            val currentSeatsToBook = uiState.seatsToBook ?: emptyList()
            val currentSeatsToReserve = uiState.seatsToReserve ?: emptyList()
            val currentPassengerList = uiState.passengerList ?: emptyList()

            val updatedSeatsToBook = currentSeatsToBook.toMutableList()
            val updatedSeatsToReserve = currentSeatsToReserve.toMutableList()
            val updatedPassengerList = currentPassengerList.toMutableList()

            val updatedSeats = uiState.seatLayout?.seats?.map { existingSeat ->
                if (existingSeat.seatNumber == seat.seatNumber) {
                    when (interaction) {
                        SeatInteraction.PRESS -> {
                            initializeFreshCustomerDetails(seat)

                            when {
                                existingSeat.isSelectedForBooking -> {
                                    // remove from book
                                    updatedSeatsToBook.removeAll { it.seatNumber == existingSeat.seatNumber }
                                    updatedPassengerList.removeAll { it.seatNumberSelected == existingSeat.seatNumber }

                                    existingSeat.copy(
                                        isPressed = false,
                                        isSelectedForBooking = false
                                    )
                                }
                                existingSeat.isSelectedForReservation -> {
                                    // remove from reserve
                                    updatedSeatsToReserve.removeAll { it.seatNumber == existingSeat.seatNumber }
                                    existingSeat.copy(
                                        isPressed = true,
                                        isSelectedForReservation = false
                                    )
                                }
                                else -> {
                                    // TODO Add only after customer details have been submitted
                                    // Handling this in the customer details dialog after clicking done
                                    // would make more sense
//                                    updatedSeatsToBook.add(existingSeat)
                                    existingSeat.copy(
                                        isPressed = true
//                                        isSelectedForBooking = true
                                    )
                                }
                            }
                        }

                        SeatInteraction.LONG_PRESS -> {
                            when {
                                existingSeat.isSelectedForBooking -> {
                                    updatedSeatsToBook.removeAll { it.seatNumber == existingSeat.seatNumber }
                                    updatedSeatsToReserve.add(existingSeat)
                                    existingSeat.copy(
                                        isSelectedForBooking = false,
                                        isSelectedForReservation = true,
                                        isPressed = false,
                                        isLongPressed = true
                                    )
                                }
                                existingSeat.isSelectedForReservation -> {
                                    updatedSeatsToReserve.removeAll { it.seatNumber == existingSeat.seatNumber }
                                    existingSeat.copy(
                                        isSelectedForReservation = false,
                                        isLongPressed = false
                                    )
                                }
                                else -> {
                                    updatedSeatsToReserve.add(existingSeat)
                                    existingSeat.copy(
                                        isSelectedForReservation = true,
                                        isLongPressed = true
                                    )
                                }
                            }
                        }
                    }
                } else {
                    existingSeat
                }
            } ?: emptyList()

            // Return the updated state
            uiState.copy(
                seatLayout = uiState.seatLayout?.copy(seats = updatedSeats),
                seatsToBook = updatedSeatsToBook,
                seatsToReserve = updatedSeatsToReserve,
                passengerList = updatedPassengerList
            )
        }
    }

//
    fun updateFreshCustomerDetails(field: String, value: String, seat: SeatDomain? = null) {
        _uiState.update { state ->
            val customerDetails = state.freshCustomerDetails
            val updatedCustomerDetails = when (field) {
                "customerType" -> customerDetails.copy(customerType = value)
                "customerName" -> customerDetails.copy(
                    customerName = value,
                    customerNameError = validateName(value)
                )
                "customerID" -> customerDetails.copy(
                    customerID = value,
                    customerIDError = validateId(value)
                )
                "customerResidence" -> customerDetails.copy(
                    customerResidence = value,
                    customerResidenceError = validateResidence(value)
                )
                "customerPhone" -> {
                    customerDetails.copy(
                        customerPhone = value,
                        customerPhoneError = validatePhone(value)
                    )
                }
                "amountToPay" -> customerDetails.copy(
                    amountToPay = value,
                    amountToPayError = seat?.seatPrice?.let { price ->
                        validateAmount(value, price)
                    }
                )
                else -> customerDetails
            }
            // Return the updated state
            state.copy(freshCustomerDetails = updatedCustomerDetails)
        }
    }

    fun autoFillFreshCustomerDetails(customerDetails: CustomerDetails) {
        _uiState.update { state ->
            state.copy(
                freshCustomerDetails = state.freshCustomerDetails.copy(
                    customerName = customerDetails.customerName,
                    customerID = customerDetails.customerID,
                    customerResidence = customerDetails.customerResidence,
                    customerPhone = customerDetails.customerPhone,
                    amountToPay = customerDetails.amountToPay
                )
            )
        }
    }

    private fun initializeFreshCustomerDetails(seat: SeatDomain) {
        _uiState.update { state ->
            val schedule = state.selectedSchedule ?: return@update state
            val (pickupPoint, dropOffPoint) = resolvePickupAndDropOffPoints(
                schedule,
                state.cityFrom.name,
                state.cityTo.name
            )

            state.copy(
                freshCustomerDetails = CustomerDetails(
                    dateOfTravel = state.departureDate,
                    seatNumberSelected = seat.seatNumber,
                    pickupPoint = pickupPoint,
                    dropOffPoint = dropOffPoint
                )
            )
        }
    }


    //
    fun upDateCachedCustomerDetails(customerDetails: CustomerDetails) {
        _uiState.update { state ->
            state.copy(
                cachedCustomerDetails = customerDetails,
                freshCustomerDetails = CustomerDetails(
                    pickupPoint = state.selectedSchedule?.pickup_points?.get(0) ?: "",
                    dropOffPoint = state.selectedSchedule?.drop_off_points?.get(0) ?: ""
                )
            )
        }
    }

    //
    fun resetFreshCustomerDetails() {
        _uiState.update {
            it.copy(
                freshCustomerDetails = CustomerDetails(
                    pickupPoint = it.selectedSchedule?.pickup_points?.get(0) ?: "",
                    dropOffPoint = it.selectedSchedule?.drop_off_points?.get(0) ?: ""
                )
            )
        }
    }

    //
    fun validateAllFreshCustomerDetails(seatPrice: Int) {
        _uiState.update { state ->
            val customerDetails = state.freshCustomerDetails
            val updatedCustomerDetails = customerDetails.copy(
                customerNameError = validateName(customerDetails.customerName),
                customerIDError = validateId(customerDetails.customerID),
                customerResidenceError = validateResidence(
                    customerDetails.customerResidence
                ),
                customerPhoneError = validatePhone(customerDetails.customerPhone),
                amountToPayError = validateAmount(customerDetails.amountToPay, seatPrice)
            )
            state.copy(freshCustomerDetails = updatedCustomerDetails)
        }

        // Log the updated customer details
        Log.d("BookingScreenViewModel", "CustomerDetails: ${uiState.value.freshCustomerDetails}")
    }

    fun updatePassengerList(passenger: CustomerDetails) {
        _uiState.update {
            val currentList = it.passengerList ?: emptyList()
            it.copy(
                passengerList = currentList + passenger
            )
        }
    }

    fun initializeBookingRequest() {
        val passengerList = _uiState.value.passengerList?.map { passenger ->
            passenger.toPassengerDetail()
        } ?: emptyList()

        if (passengerList.isEmpty()) {
            _uiState.update {
                it.copy(
                    errorMessage = "No seats selected for booking",
                    showErrorMessageDialog = true
                )
            }
        }

        val seats = passengerList.joinToString(",") { it.seat_number }

        val totalFare = passengerList.sumOf { it.seat_price.toIntOrNull() ?: 0 }


        _uiState.update { state ->
            state.copy(
                bookingRequest = InitiateBookingRequest(
                    customer_type = state.passengerList?.get(0)?.customerType ?: "",
                    travel_date = state.departureDate,
                    pickup_location = state.cityFrom.name,
                    drop_off_location = state.cityTo.name,
                    trip_schedule_id = state.selectedSchedule?.trip_schedule_id.toString(),
                    selected_seats = seats,
                    passenger_details = passengerList,
                    reservation_status = "0",
                    currency_code = authRepository.getUserDetails().currency,
                    total_fare = totalFare.toString(),
                    boarding_point = state.passengerList?.get(0)?.pickupPoint ?: "",
                    drop_off_point = state.passengerList?.get(0)?.dropOffPoint ?: "",
                )
            )
        }

        Log.d("BookingScreenViewModel", "BookingRequest: ${uiState.value.bookingRequest}")
    }

    fun bookSelectedSeats() {
        initializeBookingRequest()
    }

    fun reserveSelectedSeats() {
        val seatsToReserve = _uiState.value.seatsToReserve
        if (seatsToReserve.isNullOrEmpty()) {
            _uiState.update {
                it.copy(
                    errorMessage = "No seats selected for reservation",
                    showErrorMessageDialog = true
                )
            }
            return
        }

        // Prepare reserve seats request
        val reserveSeatsRequest = ReserveSeatsRequest(
            travel_date = _uiState.value.departureDate,
            pickup_location = _uiState.value.cityFrom.name,
            drop_off_location = _uiState.value.cityTo.name,
            trip_schedule_id = _uiState.value.selectedSchedule?.trip_schedule_id.toString(),
            selected_seats = _uiState.value.seatsToReserve?.joinToString(",") { it.seatNumber }
        )
        _uiState.update {
            it.copy(
                isLoading = true,
                loadingMessage = "Reserving Seats"
            )
        }
        viewModelScope.launch {
            val result = bookingRepository.reserveSeats(reserveSeatsRequest)
            when (result) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadingMessage = "",
                            successMessage = result.data.message,
                            errorMessage = null,
                            showSuccessMessageDialog = true,
                            seatsToReserve = null
                        )
                    }
                    // Remove seats from list

                    Log.d("BookingScreenViewModel", "reserveSeats: ${result.data}")
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadingMessage = "",
                            errorMessage = result.message,
                            showErrorMessageDialog = true,
                            seatsToReserve = null
                        )
                    }
                    Log.d("BookingScreenViewModel", "reserveSeats: ${result.message}")
                }
            }
        }

        Log.d("BookingScreenViewModel", "reserveSeatsRequest: $reserveSeatsRequest")
    }
    fun resetShowSuccessMessageDialog() {
        _uiState.update {
            it.copy(
                showSuccessMessageDialog = false
            )
        }
        // Refresh the schedules
        getSeatsAvailable()
    }

    fun logoutUser() {
        authRepository.logoutUser()
        clearUIState()
    }

    fun clearUIState() {
        _uiState.update {
            BookingScreenUIState()
        }
    }

    fun resetUiState() {
        _uiState.update { state ->
            state.copy(
                seatsToBook = null,
                seatsToReserve = null,
                selectedSeat = null,
//                cachedCustomerDetails = null
                freshCustomerDetails = CustomerDetails(),
                passengerList = null,
                bookingRequest = null
            )
        }
    }

    fun resetSelectedSchedule() {
        _uiState.update {
            it.copy(
                selectedSchedule = null
            )
        }
        resetUiState()
    }

    override fun onCleared() {
        clearUIState()
    }
}

enum class SeatInteraction {
    PRESS,
    LONG_PRESS
}