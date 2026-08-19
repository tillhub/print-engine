package de.tillhub.printengine.html

import androidx.compose.ui.graphics.ImageBitmap
import de.tillhub.printengine.PrintService
import de.tillhub.printengine.PrinterController
import de.tillhub.printengine.barcode.BarcodeEncoder
import de.tillhub.printengine.barcode.BarcodeType
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterResult
import de.tillhub.printengine.data.PrinterServiceVersion
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.PrintingIntensity
import de.tillhub.printengine.data.PrintingPaperSpec
import de.tillhub.printengine.withPrinterCatching
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class HtmlPrinterControllerTest {
    @Test
    fun `start sends the assembled receipt`() {
        val controller = TestHtmlPrinterController()

        controller.printText("receipt")
        controller.start()

        assertTrue(controller.printedContent.orEmpty().contains("receipt"))
    }

    @Test
    fun `start sends nothing when there is nothing batched`() {
        val controller = TestHtmlPrinterController()

        controller.start()

        assertNull(controller.printedContent)
    }

    @Test
    fun `a receipt the transport rejects does not leak into the next print job`() {
        val controller = TestHtmlPrinterController(failOnPrint = true)

        controller.printText("first")
        runCatching { controller.start() }
        controller.failOnPrint = false

        controller.printText("second")
        controller.start()

        val printed = controller.printedContent.orEmpty()
        assertTrue(printed.contains("second"))
        assertTrue(!printed.contains("first"), "stale batch was resent")
    }

    @Test
    fun `a transport failure is reported as an error rather than as success`() {
        val controller = TestHtmlPrinterController(failOnPrint = true)
        val service = TestPrintService(controller)

        controller.printText("receipt")
        val result = service.withPrinterCatching { it.start() }

        assertTrue(
            result is PrinterResult.Error.WithException,
            "expected a failed transport to surface as an error but was $result",
        )
    }

    private class TestPrintService(
        controller: PrinterController,
    ) : PrintService() {
        override var printController: PrinterController? = controller
        override val printerState: Flow<PrinterState> = MutableStateFlow(PrinterState.Connected)
    }

    private class TestHtmlPrinterController(
        var failOnPrint: Boolean = false,
    ) : HtmlPrinterController(
        printerState = MutableStateFlow(PrinterState.Connected),
        barcodeEncoder = NoBarcodeEncoder,
        paperSpec = PrintingPaperSpec.PaxPaper56mm,
        barcodeSize = BarcodeSize(height = 140, width = 435),
        qrCodeSize = QrCodeSize(220),
        fontSize = FontSize(13),
        includeStyleTag = false,
        feedString = FeedString("<br />"),
    ) {
        var printedContent: String? = null
            private set

        override fun printContent(
            content: String,
            cutAfterPrint: Boolean,
        ) {
            if (failOnPrint) throw IllegalStateException("transport failed")
            printedContent = content
        }

        override fun setFontSize(fontSize: PrintingFontType) = Unit

        override fun setIntensity(intensity: PrintingIntensity) = Unit

        override fun printImage(image: ImageBitmap) = Unit

        override fun cutPaper() = Unit

        override suspend fun getPrinterInfo(): PrinterInfo = PrinterInfo(
            serialNumber = "n/a",
            deviceModel = "test",
            printerVersion = "n/a",
            printerPaperSpec = PrintingPaperSpec.PaxPaper56mm,
            printingFontType = PrintingFontType.DEFAULT_FONT_SIZE,
            printerHead = "n/a",
            printedDistance = 0,
            serviceVersion = PrinterServiceVersion.Unknown,
        )
    }

    private object NoBarcodeEncoder : BarcodeEncoder {
        override fun encodeAsBitmap(
            content: String,
            type: BarcodeType,
            imgWidth: Int,
            imgHeight: Int,
        ): ImageBitmap? = null
    }
}
