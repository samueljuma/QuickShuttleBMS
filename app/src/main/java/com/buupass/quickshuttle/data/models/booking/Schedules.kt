package com.buupass.quickshuttle.data.models.booking

import com.buupass.quickshuttle.domain.booking.SeatLayout
import kotlinx.serialization.Serializable

@Serializable
data class SchedulesResponse(
    val status: Boolean,
    val message: String,
    val trip_list: List<Schedule>
)

@Serializable
data class SeatAvailabilityResponse(
    val status: Boolean,
    val message: String,
    val schedule: Schedule
)

@Serializable
data class Schedule(
    val trip_schedule_id: Int,
    val trip_schedule_name: String? = null,
    val reporting_time: String? = null,
    val route: Route? = null,
    val departure_time: String? = null,
    val bus_capacity: Int? = null,
    val number_of_available_seats: Int,
    val pickup_points: List<String>,
    val drop_off_points: List<String>,
    val seat_types: List<SeatType>? = null,
    val seats: List<Seat>? = null,
    val reserved_seats: List<ReservedSeat>? = null,
    val bus_layout: BusLayout? = null
)

@Serializable
data class Route(
    val id: Int,
    val name: String,
)

@Serializable
data class SeatType(
    val price: Int,
    val category: String // Using backticks for "class" since it's a reserved keyword in Kotlin
)

@Serializable
data class Seat(
    val seat_id: String,
    val seat_type: String,
    val seat_price: Int
)

@Serializable
data class ReservedSeat(
    val seat_id: String,
    val seat_type: String,
    val seat_price: Int,
    val reserved_by: Int
)

@Serializable
data class BusLayout(
    val total_spaces: Int,
    val columns: Int,
    val rows: Int,
    val booked_seats: List<String>,
    val reserved_seats: List<String>,
    val seat_numbers_list: List<String>,
    val seat_numbers_string: String,
    val error_message: String
)

fun Schedule.toSeatLayout(): SeatLayout {
    return SeatLayout(
        columns = bus_layout?.columns ?: 0,
        totalSpaces = bus_layout?.total_spaces ?: 0,
        freeSeats = this.seats ?: emptyList(),
        bookedSeats = bus_layout?.booked_seats ?: emptyList(),
        reservedSeats = this.reserved_seats ?: emptyList(),
        seatNumbers = bus_layout?.seat_numbers_list ?: emptyList()
    )
}