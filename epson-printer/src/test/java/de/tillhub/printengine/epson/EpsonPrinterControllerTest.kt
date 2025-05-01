package de.tillhub.printengine.epson

import android.graphics.Bitmap
import com.epson.epos2.Epos2Exception
import com.epson.eposprint.Builder.BARCODE_CODE128
import com.epson.eposprint.Builder.COLOR_1
import com.epson.eposprint.Builder.CUT_NO_FEED
import com.epson.eposprint.Builder.FONT_A
import com.epson.eposprint.Builder.HALFTONE_DITHER
import com.epson.eposprint.Builder.HRI_BELOW
import com.epson.eposprint.Builder.MODE_MONO
import com.epson.eposprint.Builder.PARAM_DEFAULT
import de.tillhub.printengine.data.ConnectionType
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.data.PrinterInfo
import de.tillhub.printengine.data.PrinterServiceVersion
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.PrintingPaperSpec
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow

class EpsonPrinterControllerTest : FunSpec({

    lateinit var printerData: ExternalPrinter
    lateinit var printerWrapper: PrinterWrapper
    lateinit var printerState: MutableStateFlow<PrinterState>

    lateinit var controller: EpsonPrinterController

    beforeTest {
        printerData = mockk()
        printerWrapper = mockk()
        printerState = MutableStateFlow<PrinterState>(PrinterState.Preparing)
        controller = EpsonPrinterController(
            printerData = printerData,
            printerWrapper = printerWrapper,
            printerState = printerState
        )
    }
    test("printText should call addText with newline") {
        val text = "Hello, World!"
        every { printerWrapper.addText(any()) } just Runs

        controller.printText(text)

        verify { printerWrapper.addText("Hello, World!\n") }
    }

    test("printText should handle Epos2Exception and update state") {
        val text = "Hello, World!"
        val exception = Epos2Exception(123)
        every { printerWrapper.addText(any()) } throws exception
        every { printerWrapper.clearCommandBuffer() } just Runs

        controller.printText(text)

        verify {
            printerWrapper.addText("Hello, World!\n")
            printerWrapper.clearCommandBuffer()
        }
        printerState.value shouldBe EpsonPrinterErrorState.epsonExceptionToState(exception)
    }

    test("printBarcode should call addBarcode with correct parameters") {
        val barcode = "123456789"
        every { printerWrapper.addBarcode(any(), any(), any(), any(), any(), any()) } just Runs

        controller.printBarcode(barcode)

        verify {
            printerWrapper.addBarcode(
                barcode,
                BARCODE_CODE128,
                HRI_BELOW,
                FONT_A,
                4,
                200
            )
        }
        printerState.value shouldBe PrinterState.Preparing
    }

    test("printImage should call addImage with correct parameters") {
        val bitmap = mockk<Bitmap> {
            every { width } returns 100
            every { height } returns 100
        }
        every {
            printerWrapper.addImage(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } just Runs

        controller.printImage(bitmap)

        verify {
            printerWrapper.addImage(
                bitmap = bitmap,
                x = 0,
                y = 0,
                width = 100,
                height = 100,
                color = COLOR_1,
                mode = MODE_MONO,
                halftone = HALFTONE_DITHER,
                brightness = 1.0,
                compress = 2
            )
        }
        printerState.value shouldBe PrinterState.Preparing
    }

    test("start should connect and send data when not connected") {
        every { printerWrapper.status } returns PrinterStatus(connection = 0)
        every { printerWrapper.connect(any(), any()) } just Runs
        every { printerWrapper.sendData(any()) } just Runs
        every { printerWrapper.clearCommandBuffer() } just Runs
        every { printerWrapper.disconnect() } just Runs
        every { printerData.connectionType } returns ConnectionType.LAN
        every { printerData.connectionAddress } returns "192.168.192.168"

        controller.start()

        verify {
            printerWrapper.connect("TCP:192.168.192.168", PARAM_DEFAULT)
            printerWrapper.sendData(PARAM_DEFAULT)
            printerWrapper.clearCommandBuffer()
            printerWrapper.disconnect()
        }
        printerState.value shouldBe PrinterState.Preparing
    }

    test("start should handle Epos2Exception during connection") {
        val exception = Epos2Exception(123)
        every { printerWrapper.status } returns PrinterStatus(connection = 0)
        every { printerWrapper.connect(any(), any()) } throws exception
        every { printerWrapper.clearCommandBuffer() } just Runs
        every { printerWrapper.disconnect() } just Runs
        every { printerData.connectionType } returns ConnectionType.LAN
        every { printerData.connectionAddress } returns "192.168.192.168"

        controller.start()

        verify {
            printerWrapper.connect("TCP:192.168.192.168", PARAM_DEFAULT)
            printerWrapper.clearCommandBuffer()
            printerWrapper.disconnect()
        }
        printerState.value shouldBe EpsonPrinterErrorState.epsonExceptionToState(exception)
    }

    test("getPrinterInfo should return printer data info") {
        every { printerData.info } returns PrinterInfo(
            serialNumber = "n/a",
            deviceModel = "P9 pro",
            printerVersion = "n/a",
            printerPaperSpec = PrintingPaperSpec.SunmiPaper56mm,
            printingFontType = PrintingFontType.DEFAULT_FONT_SIZE,
            printerHead = "n/a",
            printedDistance = 0,
            serviceVersion = PrinterServiceVersion.Unknown
        )
        val info = controller.getPrinterInfo()
        info shouldBe PrinterInfo(
            serialNumber = "n/a",
            deviceModel = "P9 pro",
            printerVersion = "n/a",
            printerPaperSpec = PrintingPaperSpec.SunmiPaper56mm,
            printingFontType = PrintingFontType.DEFAULT_FONT_SIZE,
            printerHead = "n/a",
            printedDistance = 0,
            serviceVersion = PrinterServiceVersion.Unknown
        )
    }

    test("feedPaper should call addFeedLine") {
        every { printerWrapper.addFeedLine(any()) } just Runs

        controller.feedPaper()

        verify { printerWrapper.addFeedLine(1) }
        printerState.value shouldBe PrinterState.Preparing
    }

    test("cutPaper should call addCut") {
        every { printerWrapper.addCut(any()) } just Runs

        controller.cutPaper()

        verify { printerWrapper.addCut(CUT_NO_FEED) }
        printerState.value shouldBe PrinterState.Preparing
    }

    test("setFontSize should call addTextFont with correct font") {
        every { printerWrapper.addTextFont(any()) } just Runs

        controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)

        verify { printerWrapper.addTextFont(FONT_A) }
        printerState.value shouldBe PrinterState.Preparing
    }
})

