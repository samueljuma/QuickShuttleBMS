package com.buupass.quickshuttle.utils

import android.app.DatePickerDialog
import android.content.Context
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.buupass.quickshuttle.R
import com.buupass.quickshuttle.data.models.booking.Schedule
import com.buupass.quickshuttle.data.models.onboardingpassenger.PassengerToOnboard
import com.buupass.quickshuttle.domain.auth.UserDomain
import com.buupass.quickshuttle.domain.booking.SeatDomain
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.time.format.DateTimeParseException

fun String.isValidSeatInBusLayout(): Boolean = this != "_"

@Composable
fun SeatDomain.getSeatColor(user: UserDomain): Color {
    return if (seatNumber.isValidSeatInBusLayout()) {
        when {
//            isPressed -> MaterialTheme.colorScheme.tertiary
            isLongPressed -> colorResource(R.color.reservedSeatColor)
            isSelectedForBooking -> MaterialTheme.colorScheme.inverseSurface
            isSelectedForReservation -> colorResource(R.color.reservedSeatColor)
            isBooked -> MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.4f)
            isReserved -> {
                // check if reserved by current user(use current user id)
                // if no return inverse surface color else return reserved seat color
                if (reservedBy == user.id) {
                    colorResource(R.color.reservedSeatColor)
                } else {
                    MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.4f)
                }
            }
            else -> MaterialTheme.colorScheme.primary
        }
    } else {
        Color.Transparent
    }
}

fun LocalDate.formatted(): String {
    return this.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}

fun LocalDate.formattedWithDayOfgWeek(): String {
    val dayOfWeek = this.format(DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH)) // e.g., Tuesday
    val month = this.format(DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH))       // e.g., Sept
    val day = this.dayOfMonth
    val year = this.year
    val suffix = getDayOfMonthSuffix(day)

    return "$dayOfWeek $month ${day}$suffix $year"
}

fun getDayOfMonthSuffix(day: Int): String {
    return when {
        day in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }
}
fun LocalDateTime.formattedWithTime(): String {
    return this.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
}

fun String.getDayOfWeek(): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val outputFormat = SimpleDateFormat("EEEE", Locale.ENGLISH)
    val date: Date? = inputFormat.parse(this)
    return date?.let { outputFormat.format(it) } ?: this
}

fun String.formatTime(): String {
    return try {
        val inputFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
        val outputFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        val date = inputFormat.parse(this)
        outputFormat.format(date ?: return this)
    } catch (e: Exception) {
        this
    }
}

fun String.toFormattedAmPm(): String {
    return try {
        // Try parsing the full time with optional seconds
        val inputFormatter = DateTimeFormatter.ofPattern("[H:mm[:ss]]", Locale.getDefault())
        val time = LocalTime.parse(this, inputFormatter)

        val outputFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
        time.format(outputFormatter)
    } catch (e: DateTimeParseException) {
        "N/A"
    }
}


fun showDatePickerDialog(
    context: Context,
    today: LocalDate = todayDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val themedContext = ContextThemeWrapper(context, R.style.Theme_QuickShuttleBMS) //TODO
    val datePickerDialog = DatePickerDialog(
        themedContext,
        { _, year, month, dayOfMonth ->
            val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            onDateSelected(selectedDate)
        },
        today.year,
        today.monthValue - 1, // `-1` because DatePickerDialog months are zero-based
        today.dayOfMonth
    )
    // Set min date (disable past dates)
    datePickerDialog.datePicker.minDate = System.currentTimeMillis()

    datePickerDialog.setOnDismissListener {
        onDismiss()
    }

    // Show the dialog
    datePickerDialog.show()
}

fun UserDomain.getInitials(): String {
    val source = when {
        !full_name.isNullOrBlank() -> full_name
        !email.isNullOrBlank() -> email
        username.isNotBlank() -> username
        else -> return ""
    }
    return source.split(" ").joinToString("") { it.first().toString().uppercase() }
}

fun String.capitalizeFirstCharacter(): String {
    return this.lowercase()
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
}


@Composable
fun PaymentMethod.color(): Color {
    return when (this) {
        PaymentMethod.Mpesa -> MaterialTheme.colorScheme.tertiary
        PaymentMethod.Cash -> MaterialTheme.colorScheme.primary
    }
}

fun String.generateValidPhoneNumber(): String {
    return if (this.length == 9) {
        "0$this"
    } else {
        this
    }
}

fun String.generateValidPhoneNumberWithCode(): String {
    return when {
        this.length == 9 -> "254$this" // e.g., "712345678" → "254712345678"
        this.startsWith("0") -> "254${this.drop(1)}" // e.g., "0712345678" → "254712345678"
        this.startsWith("254") -> this // already valid
        else -> this // fallback, return as-is or handle differently if needed
    }
}


fun Number.formatAmount(): String {
    val formatter = DecimalFormat("#,##0")
    return formatter.format(this)
}

fun String.getFormatedRoute(): String{
    val destinations = this.split("-")
    return "${destinations[0]} to ${destinations[1]}"
}

fun PassengerToOnboard.getPickUpAndDropOff(): String{
    return "$pickup to $dropoff"
}

fun String.extractSeatParts(): Pair<Int, String> {
    val matchResult = Regex("(\\d+)([A-Za-z]?)").find(this)
    val number = matchResult?.groups?.get(1)?.value?.toIntOrNull() ?: 0
    val letter = matchResult?.groups?.get(2)?.value ?: ""
    return number to letter
}

fun PassengerToOnboard.seatNumberNumericPart(): Int =
    seat_number.extractSeatParts().first

fun PassengerToOnboard.seatNumberAlphaPart(): String =
    seat_number.extractSeatParts().second

fun resolvePickupAndDropOffPoints(
    schedule: Schedule,
    cityFromName: String,
    cityToName: String
): Pair<String, String> {
    val pickupPoint = schedule.pickup_points
        .firstOrNull { it.equals(cityFromName, ignoreCase = true) }
        ?: schedule.pickup_points.firstOrNull()
        ?: "Select Pickup Point"

    val dropOffPoint = schedule.drop_off_points
        .firstOrNull { it.equals(cityToName, ignoreCase = true) }
        ?: schedule.drop_off_points.firstOrNull()
        ?: "Select Drop Off Point"

    return pickupPoint to dropOffPoint
}
fun String?.getDepartureTime(): String {
    return this?.toFormattedAmPm() ?: "N/A"
}
