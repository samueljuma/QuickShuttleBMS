package com.buupass.quickshuttle.ui.screens.common.printer

import com.buupass.quickshuttle.R
import com.buupass.quickshuttle.utils.timeNow
import com.buupass.quickshuttle.utils.formattedWithTime

data class TicketDetails(
    val operatorDetails: OperatorDetails = OperatorDetails(),
    val tripDetails: TripDetails? = null,
    val termsAndConditions: Pair<String, List<String>> = operatorTermsAndConditions,
    val servedBy: String? = null,
    val reprintedBy: String? = null,
    val reportingTime: String? = null,
    val departureTime: String? = null,
    val poweredByString: String = "Powered by BuuPass.com\n",
    val printedAt: String = "Printed on: ${timeNow.formattedWithTime()}\n"
)

val string = ""

data class OperatorDetails(
    val logo : Int = R.drawable.print_logo,
    val name: String = "QuickShuttle\n",
    val contact1: String = "Kampala - 256774544319\n",
    val contact2: String = "Lira- 256775330070\n",
    val contact3: String = "Apac - 256779878632\n",
    val location: String = "Location: Kampala\n",
    val address: String = "P.O. BOX 21 Apac\n",
)

data class TripDetails(
    val bookingID: String,
    val date: String,
    val trip: String,
    val dayOfWeek: String,
    val reportingTime: String,
    val departureTime: String,
    val route: String,
    val pickUpPoint: String,
    val dropOffPoint: String,
    val passengers: List<PassengerDetails> = emptyList(),
)

data class PassengerDetails(
    val pnr: String,
    val name: String,
    val idNumber: String,
    val phoneNumber: String,
    val amount: String,
    val seatNumber: String,
    val seatPrice: String,
    val discountedPrice: String,
    val luggage: String
)

val operatorTermsAndConditions = Pair(
    "TERMS AND CONDITIONS\n",
    listOf(
        "1.The ticket is valid for the \ndate and time of travel as shown only.\n"+
            "2.The management reserves the \nright to change bus type and \nseat number issued.\n"+
            "3.Allowed hand luggage is 23kg\n"
    )
)


val sampleTicketDetails = TicketDetails(
    operatorDetails = OperatorDetails(),
    tripDetails = TripDetails(
        bookingID = "BK123456",
        date = "2025-06-12",
        trip = "Kampala to Lira",
        dayOfWeek = "Thursday",
        reportingTime = "07:30 AM",
        departureTime = "08:00 AM",
        route = "Kampala - Luweero",
        pickUpPoint = "Kampala",
        dropOffPoint = "Lira Main Stage",
        passengers = listOf(
            PassengerDetails(
                pnr = "PNR001",
                name = "John Doe",
                idNumber = "UG12345678",
                phoneNumber = "+256700000001",
                amount = "50,000 UGX",
                seatNumber = "A1",
                seatPrice = "50,000 UGX",
                discountedPrice = "45,000 UGX",
                luggage = "20kg"
            )
        )
    ),
    termsAndConditions = operatorTermsAndConditions,
    servedBy = "Cashier001",
    reprintedBy = null,
    reportingTime = "07:30 AM",
    departureTime = "08:00 AM",
    poweredByString = "Powered by BuuPass.com\n",
    printedAt = "Printed on: 2025-06-12 10:30 AM\n"
)
