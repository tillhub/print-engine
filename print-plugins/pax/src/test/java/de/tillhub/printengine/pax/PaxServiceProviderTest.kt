package de.tillhub.printengine.pax

import android.content.Context
import de.tillhub.printengine.barcode.BarcodeEncoder
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.robolectric.RobolectricTest
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk

@RobolectricTest
internal class PaxServiceProviderTest : FunSpec({

    lateinit var context: Context
    lateinit var barcode: BarcodeEncoder

    beforeTest {
        context = mockk(relaxed = true)
        barcode = mockk(relaxed = true)
    }

    test("build returns PaxPrintService instance") {
        val result = PaxServiceProvider.build(context = context, printer = null, barcode = barcode)
        result.shouldBeInstanceOf<PaxPrintService>()
    }

    test("build throws when barcode is null") {
        shouldThrow<IllegalArgumentException> {
            PaxServiceProvider.build(context = context, printer = null, barcode = null)
        }
    }
})

