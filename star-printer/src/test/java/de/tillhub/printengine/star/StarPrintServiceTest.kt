package de.tillhub.printengine.star

import StarPrintService
import android.content.Context
import com.starmicronics.stario10.PrinterDelegate
import com.starmicronics.stario10.StarIO10Exception
import de.tillhub.printengine.data.ConnectionType
import de.tillhub.printengine.data.ExternalPrinter
import de.tillhub.printengine.data.PrinterState
import de.tillhub.printengine.star.StarPrinterController
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first

class StarPrintServiceTest : FunSpec({

    lateinit var printer: ExternalPrinter
    lateinit var context: Context

    lateinit var target: StarPrintService

    beforeTest {
        context = mockk<Context>(relaxed = true)
        printer = mockk<ExternalPrinter> {
            every { connectionType } returns ConnectionType.LAN
            every { connectionAddress } returns "12345"
            every { info } returns mockk(relaxed = true)
        }
        target = StarPrintService(context, printer)
    }
    test("initial printer state should be CheckingForPrinter") {
        target.printerState.first() shouldBe PrinterState.CheckingForPrinter
    }

    test("connectionListener onReady updates state to Connected") {
        val listener = target.connectionListener

        listener.onReady()
        target.printerState.first() shouldBe PrinterState.Connected
    }

    test("connectionListener onPaperEmpty updates state to OutOfPaper") {
        val listener = target.connectionListener

        listener.onPaperEmpty()
        target.printerState.first() shouldBe PrinterState.Error.OutOfPaper
    }

    test("connectionListener onError updates state to Malfunctions") {
        val listener = target.connectionListener

        listener.onError()
        target.printerState.first() shouldBe PrinterState.Error.Malfunctions
    }

    test("connectionListener onCommunicationError updates state to ConnectionLost") {
        val listener = target.connectionListener
        val exception = mockk<StarIO10Exception>()

        listener.onCommunicationError(exception)
        target.printerState.first() shouldBe PrinterState.Error.ConnectionLost
    }

    test("printController is initialized as StarPrinterController") {
        target.printController.shouldBeInstanceOf<StarPrinterController>()
    }

})

private val StarPrintService.connectionListener: PrinterDelegate
    get() = this::class.java.getDeclaredField("connectionListener")
        .apply { isAccessible = true }
        .get(this) as PrinterDelegate
