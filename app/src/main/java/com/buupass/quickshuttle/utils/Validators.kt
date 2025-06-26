package com.buupass.quickshuttle.utils

fun validateName(name: String): String? =
    when {
        name.isBlank() -> "Name is required"
        name.length < 2 -> "Name must be at least 2 characters"
        else -> null
    }

fun validateId(id: String): String? =
    when {
        id.isBlank() -> "ID is required"
//        id.length < 2 -> "ID must be at least 2 characters"
        else -> null
    }

fun validateSenderID(id: String): String? =
    when {
        id.isBlank() -> "ID is required"
        id.length < 6 -> "ID must be at least 6 characters"
        else -> null
    }

fun validateField(id: String): String? =
    when {
        id.isBlank() -> "Cannot be blank"
        else -> null
    }

fun validateResidence(residence: String): String? =
    if (residence.isBlank()) "Residence is required" else null

fun validatePhone(phone: String): String? {
    val phoneRegex = Regex("^(?:0)?(7\\d{8}|1\\d{8})$")
    return when {
        phone.isBlank() -> "Phone number is required"
        !phoneRegex.matches(phone) -> "Invalid phone number"
        else -> null
    }
}

fun validateAmount(amount: String, seatPrice: Int): String? {
    val parsed = amount.toIntOrNull()
    return when {
        amount.isBlank() -> "Amount is required"
        parsed == null -> "Amount must be a number"
        parsed < seatPrice -> "Amount is less"
        else -> null
    }
}
fun validateDiscount(amount: String): String? {
    val parsed = amount.toIntOrNull()
    return when {
        amount.isBlank() -> "Amount is required"
        parsed == null -> "Amount must be a number"
        parsed > PASSENGER_DISCOUNT -> "Discount cannot be more than $PASSENGER_DISCOUNT"
        else -> null
    }
}