package com.buupass.quickshuttle.data.models.reprint

import com.buupass.quickshuttle.domain.auth.UserDomain
import com.buupass.quickshuttle.ui.screens.common.printer.PassengerDetails
import com.buupass.quickshuttle.ui.screens.common.printer.TicketDetails
import com.buupass.quickshuttle.ui.screens.common.printer.TripDetails
import com.buupass.quickshuttle.utils.formatTime
import com.buupass.quickshuttle.utils.getDayOfWeek

fun FetchTicketResponse.toTicketDetails(
    currentUser: UserDomain
): TicketDetails? {
    val data = this.data ?: return null
    val passengers = data.passengers
    if (passengers.isEmpty()) return null

    val passengerList = passengers.map { passenger ->
        PassengerDetails(
            pnr = passenger.pnr_number,
            name = passenger.name,
            idNumber = passenger.id_number,
            phoneNumber = passenger.phone_number,
            amount = passenger.seat_price.toString(),
            seatNumber = passenger.seat_number,
            seatPrice = passenger.seat_price.toString(),
            luggage = passenger.luggage.toString(),
            discountedPrice = passenger.discounted_price.toString()
        )
    }

    return TicketDetails(
        servedBy = data.booked_by,
        reprintedBy = currentUser.full_name,
        tripDetails = TripDetails(
            bookingID = data.booking_id,
            date = data.travel_date,
            dayOfWeek = data.travel_date.getDayOfWeek(),
            reportingTime = data.reporting_time.formatTime(),
            departureTime = data.departure_time.formatTime(),
            route = data.route,
            pickUpPoint = data.pickup_location,
            dropOffPoint = data.drop_off_location,
            passengers = passengerList,
            trip = data.trip
        )
    )
}