package de.tillhub.printengine.verifone

import android.content.Context
import de.tillhub.printengine.barcode.BarcodeEncoder
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.robolectric.RobolectricTest
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk

@RobolectricTest
internal class VerifoneServiceProviderTest :
    FunSpec({

        lateinit var context: Context
        lateinit var barcode: BarcodeEncoder

        beforeTest {
            context = mockk(relaxed = true)
            barcode = mockk(relaxed = true)
        }

        test("build returns VerifonePrintService instance when externalPrinter is null and barcode is provided") {
            val result = VerifoneServiceProvider.build(context = context, printer = null, barcode = barcode)
            result.shouldBeInstanceOf<VerifonePrintService>()
        }

        test("build throws when barcode is null") {
            shouldThrow<IllegalArgumentException> {
                VerifoneServiceProvider.build(context = context, printer = null, barcode = null)
            }
        }
    })
