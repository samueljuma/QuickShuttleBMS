package com.buupass.quickshuttle.data.models.booking

import com.buupass.quickshuttle.data.models.payment.PassengerDetail
import com.buupass.quickshuttle.utils.generateValidPhoneNumber
import com.buupass.quickshuttle.utils.generateValidPhoneNumberWithCode
import kotlinx.serialization.Serializable

@Serializable
data class CustomerDetails(
    val customerType: String = "Normal",
    val customerName: String = "",
    val customerID: String = "",
    val customerResidence: String = "",
    val customerPhone: String = "",
    val seatNumberSelected: String = "",
    val amountToPay: String = "",
    val pickupPoint: String = "",
    val dropOffPoint: String = "",
    val dateOfTravel: String = "",
    val customerNameError: String? = null,
    val customerIDError: String? = null,
    val customerResidenceError: String? = null,
    val customerPhoneError: String? = null,
    val amountToPayError: String? = null,
) {
    private val noBlankFields = customerName.isNotBlank() &&
        customerID.isNotBlank() &&
        customerPhone.isNotBlank() && amountToPay.isNotBlank()

    val isValid = listOf(
        customerNameError,
        customerIDError,
        customerPhoneError,
        amountToPayError,
    ).all { it == null } && noBlankFields
}

fun CustomerDetails.toPassengerDetail(): PassengerDetail {
    return PassengerDetail(
        id_number = customerID,
        kode = "0",
        luggage_price = "0",
        name = customerName,
        phone = customerPhone.generateValidPhoneNumber(),
        residence = customerResidence,
        seat_number = seatNumberSelected,
        seat_price = amountToPay,
        customer_type = customerType
    )
}