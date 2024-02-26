package de.tillhub.printengine.barcode

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.robolectric.RobolectricTest
import io.kotest.matchers.shouldBe

@RobolectricTest
class BarcodeEncoderImplTest : FunSpec({

    val barcodeEncoder: BarcodeEncoder = BarcodeEncoderImpl()

    test("encodeAsBitmap barcode") {
        val result = barcodeEncoder.encodeAsBitmap("barcode", BarcodeType.CODE_128, 500, 150)

        val expectedResult: BitMatrix =
            MultiFormatWriter().encode(
                "barcode",
                BarcodeFormat.CODE_128,
                500,
                150,
                emptyMap<EncodeHintType, Any>()
            )

        val width = expectedResult.width
        val height = expectedResult.height
        for (y in 0 until height) {
            for (x in 0 until width) {
                result?.getPixel(x, y) shouldBe if (expectedResult.get(x, y)) BLACK else WHITE
            }
        }
    }

    test("encodeAsBitmap qr code") {
        val result = barcodeEncoder.encodeAsBitmap("qrcode", BarcodeType.QR_CODE, 500, 500)

        val expectedResult: BitMatrix =
            MultiFormatWriter().encode(
                "qrcode",
                BarcodeFormat.QR_CODE,
                500,
                500,
                HashMap<EncodeHintType, Any>().apply {
                    put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q)
                }
            )

        val width = expectedResult.width
        val height = expectedResult.height
        for (y in 0 until height) {
            for (x in 0 until width) {
                result?.getPixel(x, y) shouldBe if (expectedResult.get(x, y)) BLACK else WHITE
            }
        }
    }
}) {
    companion object {
        const val BLACK: Int = 0xFF000000.toInt()
        const val WHITE: Int = 0xFFFFFFFF.toInt()
    }
}
