package de.tillhub.printengine.verifone

import android.content.Context
import de.tillhub.printengine.data.ExternalPrinter
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.robolectric.RobolectricTest
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk

@RobolectricTest
internal class VerifoneServiceProviderTest : FunSpec({

    lateinit var context: Context

    beforeTest {
        context = mockk(relaxed = true)
    }

    test("build returns VerifonePrintService instance when externalPrinter is null") {
        val result = VerifoneServiceProvider.build(context, null)
        result.shouldBeInstanceOf<VerifonePrintService>()
    }

    test("build returns VerifonePrintService instance when externalPrinter is non-null") {
        val externalPrinter = mockk<ExternalPrinter>()
        val result = VerifoneServiceProvider.build(context, externalPrinter)
        result.shouldBeInstanceOf<VerifonePrintService>()
    }
})
