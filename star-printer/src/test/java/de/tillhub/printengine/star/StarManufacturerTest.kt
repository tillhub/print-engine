package de.tillhub.printengine.star

import android.content.Context
import de.tillhub.printengine.data.ConnectionType
import de.tillhub.printengine.data.ExternalPrinter
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk

class StarManufacturerTest : FunSpec({

    test("build should return StarPrintService") {
        val context = mockk<Context>(relaxed = true)
        val printer = mockk<ExternalPrinter>(relaxed = true) {
            every { connectionType } returns ConnectionType.LAN
        }

        val result = StarManufacturer.build(context, printer)

        result.shouldBeInstanceOf<StarPrintService>()
    }
})
