package de.tillhub.printengine.star

import android.graphics.Bitmap
import com.starmicronics.stario10.StarIO10ErrorCode
import com.starmicronics.stario10.StarIO10Exception
import com.starmicronics.stario10.StarIO10IllegalHostDeviceStateException
import com.starmicronics.stario10.StarPrinter
import com.starmicronics.stario10.starxpandcommand.DocumentBuilder
import com.starmicronics.stario10.starxpandcommand.PrinterBuilder
import com.starmicronics.stario10.starxpandcommand.StarXpandCommandBuilder
import com.starmicronics.stario10.starxpandcommand.printer.Alignment
import com.starmicronics.stario10.starxpandcommand.printer.CutType
import com.starmicronics.stario10.starxpandcommand.printer.FontType
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.data.PrintingFontType
import de.tillhub.printengine.data.RawPrinterData
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class StarPrinterControllerTest : FunSpec({
    lateinit var starPrinter: StarPrinter
    lateinit var printerState: MutableStateFlow<PrinterState>
    lateinit var printerBuilder: PrinterBuilder
    lateinit var commandBuilder: StarXpandCommandBuilder
    lateinit var documentBuilder: DocumentBuilder
    lateinit var commandBuilderFactory: () -> StarXpandCommandBuilder
    lateinit var documentBuilderFactory: () -> DocumentBuilder
    lateinit var testDispatcher: TestDispatcher
    lateinit var controller: StarPrinterController

    beforeEach {
        testDispatcher = UnconfinedTestDispatcher()
        starPrinter = mockk(relaxed = true) {
            coEvery { openAsync().await() } returns mockk()
            coEvery { printAsync(any()).await() } returns mockk()
            coEvery { closeAsync().await() } returns mockk()
            coEvery { printRawDataAsync(any()).await() } returns mockk()
        }
        printerState = MutableStateFlow(PrinterState.CheckingForPrinter)
        printerBuilder = mockk(relaxed = true) {
            every { actionPrintText(any()) } returns this
            every { styleAlignment(any()) } returns this
            every { actionPrintBarcode(any()) } returns this
            every { actionPrintImage(any()) } returns this
            every { actionPrintQRCode(any()) } returns this
            every { actionCut(any()) } returns this
        }
        commandBuilder = mockk(relaxed = true)
        documentBuilder = mockk(relaxed = true)

        commandBuilderFactory = { commandBuilder }
        documentBuilderFactory = { documentBuilder }

        controller = StarPrinterController(
            starPrinter,
            printerState,
            commandBuilderFactory,
            documentBuilderFactory,
            TestScope(testDispatcher),
            printerBuilder,
        )
    }

    test("observePrinterState returns correct StateFlow") {
        controller.observePrinterState() shouldBe printerState
    }

    test("sendRawData executes printer operations in correct order") {
        val rawData = RawPrinterData(byteArrayOf(1, 2, 3))

        controller.sendRawData(rawData)

        coVerifyOrder {
            starPrinter.openAsync().await()
            starPrinter.printRawDataAsync(rawData.bytes.toList())
            starPrinter.closeAsync().await()
        }
    }

    test("setFontSize applies correct font size") {
        every { printerBuilder.styleFont(any()) } returns printerBuilder

        controller.setFontSize(PrintingFontType.DEFAULT_FONT_SIZE)

        verify { printerBuilder.styleFont(FontType.A) }
    }

    test("printText adds text and feed line") {
        controller.printText("Test Text")

        verifyOrder {
            printerBuilder.actionPrintText("Test Text")
            printerBuilder.styleAlignment(Alignment.Center)
        }
    }

    test("printBarcode adds barcode with correct parameters") {
        controller.printBarcode("123456")

        verifyOrder {
            printerBuilder.actionPrintBarcode(any())
            printerBuilder.styleAlignment(Alignment.Center)
        }
    }

    test("printQr adds QR code with correct parameters") {
        every { printerBuilder.actionPrintQRCode(any()) } returns printerBuilder

        controller.printQr("test-qr")

        verifyOrder {
            printerBuilder.actionPrintQRCode(any())
            printerBuilder.styleAlignment(Alignment.Center)
        }
    }

    test("printImage adds image with correct parameters") {
        val bitmap = mockk<Bitmap>()

        controller.printImage(bitmap)

        verifyOrder {
            printerBuilder.actionPrintImage(any())
            printerBuilder.styleAlignment(Alignment.Center)
        }
    }

    test("start executes printing sequence and resets builder on success") {
        every { commandBuilder.addDocument(any()) } returns commandBuilder
        every { commandBuilder.getCommands() } returns "commands"
        every { documentBuilder.addPrinter(printerBuilder) } returns documentBuilder

        controller.start()

        coVerifyOrder {
            starPrinter.openAsync().await()
            starPrinter.printAsync("commands")
            starPrinter.closeAsync().await()
        }
    }

    test("start updates printerState on failure with UsbUnavailable error") {
        val exception = mockk<StarIO10Exception>(relaxed = true) {
            every {
                errorCode
            } returns StarIO10ErrorCode.UsbUnavailable
        }
        coEvery { starPrinter.printAsync(any()).await() } throws exception
        every { commandBuilder.addDocument(any()) } returns commandBuilder
        every { commandBuilder.getCommands() } returns "commands"
        every { documentBuilder.addPrinter(printerBuilder) } returns documentBuilder

        controller.start()

        coVerifyOrder {
            starPrinter.openAsync().await()
            starPrinter.printAsync("commands")
            starPrinter.closeAsync().await()
        }

        val expectedErrorState = StarPrinterErrorState.convert(
            StarPrinterErrorState.fromCode(StarPrinterErrorCodes.USB_UNAVAILABLE_CODE)
        )
        printerState.value shouldBe expectedErrorState
    }

    test("feedPaper adds single feed line") {
        every { printerBuilder.actionFeedLine(any()) } returns printerBuilder

        controller.feedPaper()

        verify { printerBuilder.actionFeedLine(1) }
    }

    test("cutPaper adds partial cut") {
        every { printerBuilder.actionCut(any()) } returns printerBuilder

        controller.cutPaper()

        verify { printerBuilder.actionCut(CutType.Partial) }
    }
})
