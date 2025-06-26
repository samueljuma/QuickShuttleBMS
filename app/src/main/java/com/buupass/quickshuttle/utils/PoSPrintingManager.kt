package com.buupass.quickshuttle.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import com.buupass.quickshuttle.R
import com.buupass.quickshuttle.ui.screens.common.printer.TicketDetails
import java.com.ctk.sdk.PosApiHelper
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.buupass.quickshuttle.domain.parcel.ParcelReceiptDetails
import com.buupass.quickshuttle.domain.parcel.ParcelStickerDetails

const val ALIGN_LEFT = 0
const val ALIGN_CENTER = 1
const val ALIGN_RIGHT = 2
const val BOLD = 1
const val NORMAL = 0

enum class FontSize { HUGE, NORMAL, MEDIUM }
enum class Align { LEFT, CENTER, RIGHT }

fun wrapText(text: String, maxLength: Int): List<String> {
    val words = text.split(" ")
    val result = mutableListOf<String>()
    var currentLine = StringBuilder()

    for (word in words) {
        if (currentLine.isEmpty()) {
            currentLine.append(word)
        } else if ((currentLine.length + 1 + word.length) <= maxLength) {
            currentLine.append(" ").append(word)
        } else {
            result.add(currentLine.toString().trim())
            currentLine = StringBuilder(word)
        }
    }

    if (currentLine.isNotEmpty()) {
        result.add(currentLine.toString().trim())
    }

    return result
}



fun getMaxChars(fontSize: FontSize): Int {
    return when (fontSize) {
        FontSize.HUGE -> 20
        FontSize.MEDIUM -> 23
        FontSize.NORMAL -> 30
    }
}

fun printText(
    posApiHelper: PosApiHelper,
    text: String,
    isBold: Boolean = false,
    fontSize: FontSize = FontSize.NORMAL,
    align: Align = Align.LEFT,
    wrap: Boolean = false
) {
    // Set bold
    posApiHelper.PrintSetBold(if (isBold) BOLD else NORMAL)

    // Set font
    when (fontSize) {
        FontSize.HUGE -> posApiHelper.PrintSetFont(32.toByte(), 32.toByte(), 0x00.toByte())
        FontSize.NORMAL -> posApiHelper.PrintSetFont(24.toByte(), 24.toByte(), 0x00.toByte())
        FontSize.MEDIUM -> posApiHelper.PrintSetFont(16.toByte(), 16.toByte(), 0x33.toByte())
    }

    // Set alignment
    when (align) {
        Align.LEFT -> posApiHelper.PrintSetAlign(ALIGN_LEFT)
        Align.CENTER -> posApiHelper.PrintSetAlign(ALIGN_CENTER)
        Align.RIGHT -> posApiHelper.PrintSetAlign(ALIGN_RIGHT)
    }

    // Handle wrapping
    if (wrap) {
        wrapText(text, getMaxChars(fontSize)).forEach { line ->
            posApiHelper.PrintStr(line)
        }
    } else {
        posApiHelper.PrintStr(text)
    }
}

fun printPassengerTicket(
    posApiHelper: PosApiHelper,
    ticketDetails: TicketDetails,
    context: Context
) {
    val passengers = ticketDetails.tripDetails?.passengers ?: return

    val originalLogoBitMap = BitmapFactory.decodeResource(context.resources, R.drawable.print_logo)
    val resizedLogoBitMap = originalLogoBitMap.scale(LOGO_PRINT_WIDTH, LOGO_PRINT_HEIGHT, false)

    for (passenger in passengers) {
        // Print Logo
        posApiHelper.PrintSetAlign(1) // Center Alignment
        posApiHelper.PrintBmp(resizedLogoBitMap.toMonochromeBitmap())
        posApiHelper.PrintStr("\n")
        // Operator Details
        printText(posApiHelper, ticketDetails.operatorDetails.name, isBold = true, fontSize = FontSize.HUGE, align = Align.CENTER)
        posApiHelper.PrintStr("\n")
        printText(posApiHelper, ticketDetails.operatorDetails.contact1, align = Align.CENTER)
        printText(posApiHelper, ticketDetails.operatorDetails.contact2, align = Align.CENTER)
        printText(posApiHelper, ticketDetails.operatorDetails.contact3, align = Align.CENTER)
        printText(posApiHelper, ticketDetails.operatorDetails.address,align = Align.CENTER)
        printText(posApiHelper, ticketDetails.operatorDetails.location, align = Align.CENTER)

        // Trip Details
        posApiHelper.PrintStr("\n")
        printText(posApiHelper, "Ticket Number: ${ticketDetails.tripDetails.bookingID}")
        printText(posApiHelper, "PNR: ${passenger.pnr}")
        printText(posApiHelper, "Route: ${ticketDetails.tripDetails.route}")
        printText(posApiHelper, "Date: ${ticketDetails.tripDetails.date} ${ticketDetails.tripDetails.dayOfWeek}")

        posApiHelper.PrintStr("\n")
        printText(posApiHelper, "Reporting Time: ${ticketDetails.reportingTime}")
        printText(posApiHelper, "Departure Time: ${ticketDetails.departureTime}")

        // Passenger Info
        posApiHelper.PrintStr("\n")
        printText(posApiHelper, "Name.......: ${passenger.name}")
        printText(posApiHelper, "Phone No...: ${passenger.phoneNumber}")
        printText(posApiHelper, "Amount.....: ${passenger.amount}")
        printText(posApiHelper, "Pick Up....: ${ticketDetails.tripDetails.pickUpPoint}")
        printText(posApiHelper, "Drop Off...: ${ticketDetails.tripDetails.dropOffPoint}")
        printText(posApiHelper, "Seat No....: ${passenger.seatNumber}")

        // Terms and Conditions
        posApiHelper.PrintStr("\n")
        printText(posApiHelper, ticketDetails.termsAndConditions.first, isBold = true, align = Align.CENTER)
        ticketDetails.termsAndConditions.second.forEach {
            printText(posApiHelper, it)
        }

        // Served / Reprinted By
        posApiHelper.PrintStr("\n")
        ticketDetails.servedBy?.let {
            printText(posApiHelper, "Served By: $it", align = Align.CENTER)
        }
        ticketDetails.reprintedBy?.let {
            printText(posApiHelper, "Reprinted By: $it", align = Align.CENTER)
        }

        // Footer
        printText(posApiHelper, ticketDetails.printedAt, align = Align.CENTER)
        printText(posApiHelper, ticketDetails.poweredByString, align = Align.CENTER)

        if (!ticketDetails.reprintedBy.isNullOrEmpty()) {
            printText(posApiHelper, "NB - This is a Reprint Ticket", align = Align.CENTER)
        }

        // Feed lines after each ticket
        for (i in 1..7) {
            posApiHelper.PrintStr("\n")
        }
    }

    posApiHelper.PrintStart() // Start printing job

}

fun printParcelReceipt(
    posApiHelper: PosApiHelper,
    parcelReceiptDetails: ParcelReceiptDetails?,
    context: Context
) {
    if (parcelReceiptDetails == null) return

    val originalLogoBitMap = BitmapFactory.decodeResource(context.resources, parcelReceiptDetails.operatorDetails.logo)
    val resizedLogoBitMap = originalLogoBitMap.scale(LOGO_PRINT_WIDTH, LOGO_PRINT_HEIGHT, false)
    val monoBitmap = resizedLogoBitMap.toMonochromeBitmap()

    posApiHelper.PrintSetAlign(1) // Center Alignment
    posApiHelper.PrintBmp(monoBitmap)
    printText(posApiHelper, parcelReceiptDetails.operatorDetails.name, isBold = true, fontSize = FontSize.HUGE, align = Align.CENTER)
    posApiHelper.PrintStr("\n")

    // Contact details
    printText(posApiHelper, parcelReceiptDetails.operatorDetails.contact1, align = Align.CENTER)
    printText(posApiHelper, parcelReceiptDetails.operatorDetails.contact2, align = Align.CENTER)
    printText(posApiHelper, parcelReceiptDetails.operatorDetails.contact3, align = Align.CENTER)
    printText(posApiHelper, parcelReceiptDetails.operatorDetails.address, align = Align.CENTER)
    printText(posApiHelper, parcelReceiptDetails.operatorDetails.location, align = Align.CENTER)
    posApiHelper.PrintStr("\n")

    // Route and date
    val route = "${parcelReceiptDetails.pickupLocation.uppercase()} TO ${parcelReceiptDetails.dropOffLocation.uppercase()}"
    printText(posApiHelper, route, fontSize = FontSize.NORMAL)
    printText(posApiHelper, "Date: ${parcelReceiptDetails.date}")

    // Sender and Receiver
    printText(posApiHelper, "Sender: ${parcelReceiptDetails.senderName}")
    printText(posApiHelper, "Phone Number: ${parcelReceiptDetails.senderPhoneNUmber}")
    printText(posApiHelper, "Receiver: ${parcelReceiptDetails.receiverName}")
    printText(posApiHelper, "Phone Number: ${parcelReceiptDetails.receiverPhoneNumber}")
    posApiHelper.PrintStr("\n")

    // Parcel items
    for (item in parcelReceiptDetails.parcelItems) {
        printText(posApiHelper, "${item.quantity} Items: ${item.content}")
    }
    printText(posApiHelper, "Waybill No.: ${parcelReceiptDetails.waybill}")
    printText(posApiHelper, "Parcel REF.: ${parcelReceiptDetails.parcelRef}")

    // Total cost
    printText(posApiHelper, "Total Cost Ksh. ${parcelReceiptDetails.totalCost}")
    posApiHelper.PrintStr("\n")

    printText(posApiHelper, "Served by ${parcelReceiptDetails.servedBy}", align = Align.CENTER)
    printText(posApiHelper, parcelReceiptDetails.printedAt, align = Align.CENTER)
    printText(posApiHelper, parcelReceiptDetails.tAndCs, align = Align.CENTER)
    printText(posApiHelper, parcelReceiptDetails.poweredByString, align = Align.CENTER)

    // Final few blank lines for clean paper cut
    for (i in 1..7) {
        posApiHelper.PrintStr("\n")
    }

    // Start actual print job
    posApiHelper.PrintStart()
}

fun printParcelStickers(
    posApiHelper: PosApiHelper,
    parcelStickerDetails: ParcelStickerDetails?,
    context: Context
) {
    if (parcelStickerDetails == null) return

    val parcelItems = parcelStickerDetails.parcelItems

    val originalLogoBitMap = BitmapFactory.decodeResource(context.resources, parcelStickerDetails.operatorDetails.logo)
    val resizedLogoBitMap = originalLogoBitMap.scale(LOGO_PRINT_WIDTH, LOGO_PRINT_HEIGHT, false)
    val monoBitmap = resizedLogoBitMap.toMonochromeBitmap()

    for (item in parcelItems) {
        posApiHelper.PrintSetAlign(1) // Center Alignment
        posApiHelper.PrintBmp(monoBitmap)
        printText(posApiHelper, parcelStickerDetails.operatorDetails.name, isBold = true, fontSize = FontSize.HUGE, align = Align.CENTER)
        posApiHelper.PrintStr("\n")

        val route = "${parcelStickerDetails.pickupLocation.uppercase()} TO ${parcelStickerDetails.dropOffLocation.uppercase()}"
        printText(posApiHelper, route, fontSize = FontSize.NORMAL, align = Align.CENTER)
        // Sender
        printText(posApiHelper, "Sender.: ${parcelStickerDetails.senderName}", align = Align.LEFT)
        printText(posApiHelper, "Phone Number.: ${parcelStickerDetails.senderPhoneNUmber}", align = Align.LEFT)
        // Receiver
        printText(posApiHelper, "Receiver.: ${parcelStickerDetails.receiverName}", align = Align.LEFT)
        printText(posApiHelper, "Phone Number.: ${parcelStickerDetails.receiverPhoneNumber}", align = Align.LEFT)
        // Payment & Amount
        printText(posApiHelper, "Payment Type.: ${parcelStickerDetails.paymentType}", align = Align.LEFT)
        printText(posApiHelper, "Amount.: ${parcelStickerDetails.amount}", align = Align.LEFT)
        // QR Code
        posApiHelper.PrintSetAlign(1)
        val qrCode = BitmapHelper.encodeAsBitmap(item.parcel_item_code)
        qrCode?.let {
            posApiHelper.PrintBmp(it.toMonochromeBitmap())
        }
        // References
        printText(posApiHelper, "REF.: ${item.parcel_item_code}", align = Align.CENTER)
        printText(posApiHelper, "Waybill No.: ${item.waybill}", align = Align.CENTER)

        for (i in 1..7) {
            posApiHelper.PrintStr("\n")
        }

        // Start actual print job
        posApiHelper.PrintStart()
    }
}



fun Bitmap.toMonochromeBitmap(): Bitmap {
    val width = width
    val height = height
    val monoBitmap = createBitmap(width, height)

    for (y in 0 until height) {
        for (x in 0 until width) {
            val pixel = getPixel(x, y)
            val r = (pixel shr 16) and 0xff
            val g = (pixel shr 8) and 0xff
            val b = pixel and 0xff
            val gray = (r + g + b) / 3
            val newPixel = if (gray > 160) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
            monoBitmap[x, y] = newPixel
        }
    }
    return monoBitmap
}







