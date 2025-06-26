package com.buupass.quickshuttle.utils

import com.buupass.quickshuttle.data.models.City
import com.buupass.quickshuttle.data.models.parcel.ParcelItemDto
import com.buupass.quickshuttle.data.models.parcel.ReceiptParcelItem
import com.buupass.quickshuttle.domain.parcel.ParcelReceiptDetails
import com.buupass.quickshuttle.domain.parcel.ParcelStickerDetails
import com.buupass.quickshuttle.ui.screens.common.printer.OperatorDetails


val cityList = listOf(
    City(1, "Nairobi"),
    City(2, "Mombasa"),
    City(3, "Kisumu"),
    City(4, "Nakuru"),
    City(5, "Eldoret"),
    City(6, "Thika"),
    City(7, "Malindi"),
    City(8, "Kitale"),
    City(9, "Garissa"),
    City(10, "Nyeri"),
    City(11, "Meru"),
    City(12, "Embu"),
    City(13, "Kericho"),
    City(14, "Kakamega"),
    City(15, "Machakos"),
    City(16, "Naivasha"),
    City(17, "Lamu"),
    City(18, "Isiolo"),
    City(19, "Bungoma"),
    City(20, "Voi")
)


val sampleParcelReceiptDetails = ParcelReceiptDetails(
    operatorDetails = OperatorDetails(),
    pickupLocation = "Kampala",
    dropOffLocation = "Lira Branch",
    senderName = "John Sender",
    senderPhoneNUmber = "+256700111222",
    receiverName = "Jane Receiver",
    receiverPhoneNumber = "+256700333444",
    parcelItems = listOf(
        ReceiptParcelItem(
            parcel_item_code = "ITEM001",
            waybill = "WB123456",
            content = "Electronics",
            is_dispatched = true,
            is_received = false,
            is_issued = false,
            quantity = 2,
            slug = "electronics-001"
        )
    ),
    waybill = "WB123456",
    parcelRef = "PR456789",
    totalCost = "150,000 UGX",
    date = todayDate.formatted(),  // or you can put "2025-06-12" for static sample
    poweredByString = "Powered by buupass.com\n",
    printedAt = "Printed on: ${timeNow.formattedWithTime()}\n", // or "Printed on: 2025-06-12 10:30 AM\n"
    tAndCs = "Terms & conditions apply\n",
    servedBy = "Cashier001"
)

val sampleParcelStickerDetails = ParcelStickerDetails(
    operatorDetails = OperatorDetails(),
    pickupLocation = "Kampala",
    dropOffLocation = "Gulu Branch",
    senderName = "Alice Sender",
    senderPhoneNUmber = "+256701111222",
    receiverName = "Bob Receiver",
    receiverPhoneNumber = "+256702333444",
    parcelItems = listOf(
        ParcelItemDto(
            parcel_item_code = "STICKER001",
            content = "Laptop",
            waybill = "WB890123",
            quantity = 1,
            weight = "2.5kg"
        )
    ),
    paymentType = "Cash",
    amount = "75,000 UGX",
    waybill = "WB890123"
)





