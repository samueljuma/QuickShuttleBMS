package com.buupass.quickshuttle.utils

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix

object BitmapHelper {

    private val hexStr = "0123456789ABCDEF"
    private val binaryArray = arrayOf(
        "0000", "0001", "0010", "0011",
        "0100", "0101", "0110", "0111",
        "1000", "1001", "1010", "1011",
        "1100", "1101", "1110", "1111"
    )

    fun decodeBitmap(bmp: Bitmap): ByteArray? {
        val bmpWidth = bmp.width
        val bmpHeight = bmp.height

        val list = mutableListOf<String>()
        val zeroCount = bmpWidth % 8
        val zeroStr = "0".repeat(if (zeroCount > 0) 8 - zeroCount else 0)
        val bitLen = if (zeroCount > 0) bmpWidth / 8 + 1 else bmpWidth / 8

        for (i in 0 until bmpHeight) {
            val sb = StringBuilder()
            for (j in 0 until bmpWidth) {
                val color = bmp.getPixel(j, i)
                val r = (color shr 16) and 0xff
                val g = (color shr 8) and 0xff
                val b = color and 0xff
                sb.append(if (r > 160 && g > 160 && b > 160) "0" else "1")
            }
            if (zeroCount > 0) sb.append(zeroStr)
            list.add(sb.toString())
        }

        val bmpHexList = binaryListToHexStringList(list)

        var widthHexString = Integer.toHexString(bitLen)
        if (widthHexString.length == 1) widthHexString = "0$widthHexString"
        widthHexString += "00"

        var heightHexString = Integer.toHexString(bmpHeight)
        if (heightHexString.length == 1) heightHexString = "0$heightHexString"
        heightHexString += "00"

        val commandList = mutableListOf<String>()
        commandList.add("1D763000$widthHexString$heightHexString")
        commandList.addAll(bmpHexList)

        return hexList2Byte(commandList)
    }

    private fun binaryListToHexStringList(list: List<String>): List<String> {
        return list.map { binaryStr ->
            val sb = StringBuilder()
            for (i in binaryStr.indices step 8) {
                val str = binaryStr.substring(i, i + 8)
                sb.append(myBinaryStrToHexString(str))
            }
            sb.toString()
        }
    }

    private fun myBinaryStrToHexString(binaryStr: String): String {
        val f4 = binaryStr.substring(0, 4)
        val b4 = binaryStr.substring(4, 8)
        val hex = buildString {
            append(binaryArray.indexOf(f4).let { hexStr[it] })
            append(binaryArray.indexOf(b4).let { hexStr[it] })
        }
        return hex
    }

    private fun hexList2Byte(list: List<String>): ByteArray {
        val commandList = list.mapNotNull { hexStringToBytes(it) }
        return sysCopy(commandList)
    }

    private fun hexStringToBytes(hexString: String): ByteArray? {
        if (hexString.isEmpty()) return null
        val upperHex = hexString.uppercase()
        val length = upperHex.length / 2
        val data = ByteArray(length)
        for (i in 0 until length) {
            val pos = i * 2
            data[i] = ((charToByte(upperHex[pos]) shl 4) or charToByte(upperHex[pos + 1])).toByte()
        }
        return data
    }

    private fun charToByte(c: Char): Int = "0123456789ABCDEF".indexOf(c)

    private fun sysCopy(srcArrays: List<ByteArray>): ByteArray {
        val totalLen = srcArrays.sumOf { it.size }
        val destArray = ByteArray(totalLen)
        var destPos = 0
        for (array in srcArrays) {
            array.copyInto(destArray, destPos)
            destPos += array.size
        }
        return destArray
    }

    @Throws(WriterException::class)
    fun encodeAsBitmap(str: String): Bitmap? {
        val result: BitMatrix
        try {
            result = MultiFormatWriter().encode(
                str,
                BarcodeFormat.QR_CODE,
                250,
                250,
                null
            )
        } catch (iae: IllegalArgumentException) {
            // Unsupported format
            return null
        }

        val w = result.width
        val h = result.height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            val offset = y * w
            for (x in 0 until w) {
                pixels[offset + x] =
                    if (result.get(x, y)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
            }
        }

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
        return bitmap
    }
}