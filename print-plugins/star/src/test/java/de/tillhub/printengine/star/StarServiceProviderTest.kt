package de.tillhub.printengine.star

import android.content.Context
import de.tillhub.printengine.data.ConnectionType
import de.tillhub.printengine.data.ExternalPrinter
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk

class StarServiceProviderTest : FunSpec({

    test("build should return StarPrintService when printer is provided") {
        val context = mockk<Context>(relaxed = true)
        val printer = mockk<ExternalPrinter>(relaxed = true) {
            every { connectionType } returns ConnectionType.LAN
        }

        val result = StarServiceProvider.build(context = context, printer = printer, barcode = null)

        result.shouldBeInstanceOf<StarPrintService>()
    }

    test("build should throw IllegalArgumentException when printer is null") {
        val context = mockk<Context>(relaxed = true)

        shouldThrow<IllegalArgumentException> {
            StarServiceProvider.build(context = context, printer = null, barcode = null)
        }
    }
})
