package de.tillhub.printengine.barcode

import android.graphics.Bitmap
import com.google.zxing.EncodeHintType
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import timber.log.Timber

internal class BarcodeEncoderImpl : BarcodeEncoder {

    override fun encodeAsBitmap(content: String, type: BarcodeType, imgWidth: Int, imgHeight: Int): Bitmap? {
        val hints = HashMap<EncodeHintType, Any>().apply {
            guessAppropriateEncoding(content)?.let { encoding ->
                put(EncodeHintType.CHARACTER_SET, encoding)
            }
            if (type == BarcodeType.QR_CODE) {
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q)
            }
        }

        val writer = MultiFormatWriter()
        val result: BitMatrix = try {
            writer.encode(content, typeConverter(type), imgWidth, imgHeight, hints)
        } catch (e: IllegalArgumentException) {
            Timber.e(e)
            return null
        }

        val width = result.width
        val height = result.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (result.get(x, y)) BLACK else WHITE
            }
        }

        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
        }
    }

    private fun typeConverter(type: BarcodeType): BarcodeFormat =
        when (type) {
            BarcodeType.CODE_128 -> BarcodeFormat.CODE_128
            BarcodeType.QR_CODE -> BarcodeFormat.QR_CODE
        }

    private fun guessAppropriateEncoding(content: CharSequence): String? {
        content.forEach {
            if (it.code > UTF_8_BOUNDARY) {
                return "UTF-8"
            }
        }
        return null
    }

    companion object {
        const val UTF_8_BOUNDARY = 0xFF
        const val BLACK: Int = 0xFF000000.toInt()
        const val WHITE: Int = 0xFFFFFFFF.toInt()
    }
}
