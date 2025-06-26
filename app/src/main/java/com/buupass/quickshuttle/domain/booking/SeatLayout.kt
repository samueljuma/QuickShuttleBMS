package com.buupass.quickshuttle.domain.booking

import com.buupass.quickshuttle.data.models.booking.ReservedSeat
import com.buupass.quickshuttle.data.models.booking.Seat

data class SeatLayout(
    val columns: Int,
    val totalSpaces: Int,
    val bookedSeats: List<String> = emptyList(),
    val reservedSeats: List<ReservedSeat> = emptyList(),
    val seatNumbers: List<String>,
    val freeSeats: List<Seat> = emptyList(),
    val seats: List<SeatDomain> = seatNumbers.map { seatNumber ->
        SeatDomain(
            seatPrice = freeSeats.find { it.seat_id == seatNumber }?.seat_price ?: 0,
            seatNumber = seatNumber,
            isReserved = reservedSeats.any { it.seat_id == seatNumber },
            isBooked = seatNumber in bookedSeats,
            reservedBy = reservedSeats.find { it.seat_id == seatNumber }?.reserved_by
        )
    }
)

data class SeatDomain(
    val seatPrice: Int = 0,
    val seatNumber: String = "",
    val isReserved: Boolean = false,
    val isBooked: Boolean = false,
    val isLongPressed: Boolean = false,
    val isPressed: Boolean = false,
    val isSelectedForBooking: Boolean = false,
    val isSelectedForReservation: Boolean = false,
    val reservedBy: Int? = null
) {
    fun isValidSeatInBusLayout() = seatNumber != "_"
    fun isAvailable(currentUserId: Int?): Boolean {
        if (isBooked) return false
        if (isReserved && reservedBy != currentUserId) return false
        return true
    }
}