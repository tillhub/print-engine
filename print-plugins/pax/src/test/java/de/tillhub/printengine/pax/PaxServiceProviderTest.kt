package de.tillhub.printengine.pax

import android.content.Context
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.robolectric.RobolectricTest
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk

@RobolectricTest
internal class PaxServiceProviderTest : FunSpec({

    lateinit var context: Context

    beforeTest {
        context = mockk(relaxed = true)
    }

    test("build returns PaxPrintService instance when externalPrinter is null") {
        val result = PaxServiceProvider.build(context)
        result.shouldBeInstanceOf<PaxPrintService>()
    }
})
