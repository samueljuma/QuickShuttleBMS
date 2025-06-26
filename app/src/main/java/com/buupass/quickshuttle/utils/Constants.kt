package com.buupass.quickshuttle.utils

import java.time.LocalDate
import java.time.LocalDateTime

enum class PaymentMethod {
    Mpesa,
    Cash;
}

val timeNow: LocalDateTime get() = LocalDateTime.now()
val todayDate: LocalDate get() = LocalDate.now()

const val LOGO_PRINT_HEIGHT = 160
const val LOGO_PRINT_WIDTH = 320

//const val BASE_URL = " https://quickshuttle.buupass.com/api/"
const val BASE_URL = " https://dev.quickshuttle.buupass.com/api/"

object PaymentErrors {
    const val PAYMENT_TIME_OUT_ERROR = "Payment Time Out Error"
    const val PAYMENT_NOT_CONFIRMED_ERROR = "Your payment has not been confirmed. Please try again."
    const val PAYMENT_FAILED_ERROR = "Payment Failed"
    const val BOOKING_NOT_FOUND_ERROR = "Booking Not Found"
}

object NetworkErrors {
    const val REQUEST_TIMEOUT_ERROR = "Request Timeout Error"
    const val NO_INTERNET_ERROR = "No Internet Error"
    const val INTERNAL_SERVER_ERROR = "Internal Server Error"
}

const val PARCEL_FLEET_TYPE = "parcel"
const val DEFAULT_CITY_ID = "-1"

const val PASSENGER_DISCOUNT = 500