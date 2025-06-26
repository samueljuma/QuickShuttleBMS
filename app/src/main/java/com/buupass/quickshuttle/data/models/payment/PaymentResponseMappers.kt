package com.buupass.quickshuttle.data.models.payment

import com.buupass.quickshuttle.data.models.booking.Schedule
import com.buupass.quickshuttle.domain.auth.UserDomain
import com.buupass.quickshuttle.ui.screens.common.printer.PassengerDetails
import com.buupass.quickshuttle.ui.screens.common.printer.TicketDetails
import com.buupass.quickshuttle.ui.screens.common.printer.TripDetails
import com.buupass.quickshuttle.utils.formatTime
import com.buupass.quickshuttle.utils.getDayOfWeek
import com.buupass.quickshuttle.utils.toFormattedAmPm

fun PaymentResponse.toTicketDetails(
    currentUser: UserDomain,
    schedule: Schedule
): TicketDetails? {
    val data = this.data ?: return null
    val passengers = data.passengers ?: return null
    if (passengers.isEmpty()) return null

    val passengersList = passengers.map { passenger ->
        PassengerDetails(
            pnr = passenger.pnr_number.orEmpty(),
            name = passenger.name.orEmpty(),
            idNumber = passenger.id_number.orEmpty(),
            phoneNumber = passenger.phone_number.orEmpty(),
            amount = passenger.seat_price?.toString() ?: "0.0",
            seatNumber = passenger.seat_number.orEmpty(),
            seatPrice = passenger.seat_price?.toString() ?: "0.0",
            luggage = passenger.luggage?.toString() ?: "0.0",
            discountedPrice = passenger.discounted_price?.toString() ?: "0.0"
        )
    }

    return TicketDetails(
        servedBy = currentUser.full_name,
        reportingTime = schedule.reporting_time?.toFormattedAmPm().orEmpty(),
        departureTime = schedule.departure_time?.toFormattedAmPm().orEmpty(),
        tripDetails = TripDetails(
            bookingID = data.booking_id.orEmpty(),
            date = data.travel_date.orEmpty(),
            dayOfWeek = data.travel_date?.getDayOfWeek().orEmpty(),
            reportingTime = data.reporting_time?.formatTime().orEmpty(),
            departureTime = data.departure_time?.formatTime().orEmpty(),
            route = data.route.orEmpty(),
            pickUpPoint = data.pickup_location.orEmpty(),
            dropOffPoint = data.drop_off_location.orEmpty(),
            passengers = passengersList,
            trip = data.trip.orEmpty()
        )
    )
}