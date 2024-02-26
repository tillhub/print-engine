package de.tillhub.printengine.data

import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.robolectric.RobolectricTest
import io.kotest.matchers.shouldBe
import org.robolectric.shadows.ShadowBuild

@RobolectricTest
class PrinterManufacturerTest : FunSpec({

    test("SUNMI device") {
        ShadowBuild.setManufacturer("SUNMI")
        PrinterManufacturer.get() shouldBe PrinterManufacturer.SUNMI
    }

    test("PAX device") {
        ShadowBuild.setManufacturer("PAX")
        PrinterManufacturer.get() shouldBe PrinterManufacturer.PAX
    }

    test("Verifone device") {
        ShadowBuild.setManufacturer("Verifone")
        PrinterManufacturer.get() shouldBe PrinterManufacturer.VERIFONE
    }
})
