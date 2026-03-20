package de.tillhub.printengine.epson

import android.content.Context
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EpsonServiceProviderTest {

    private lateinit var context: Context

    @BeforeTest
    fun before() {
        context = mockk(relaxed = true)
    }

    @Test
    fun buildShouldThrowWhenPrinterIsNull() {
        // When
        val exception = assertFailsWith<IllegalArgumentException> {
            EpsonServiceProvider.build(context, printer = null)
        }

        // Then
        assertEquals(
            "EpsonServiceProvider requires an ExternalPrinter configuration",
            exception.message,
        )
    }

    @Test
    fun buildShouldThrowWhenPrinterIsNullWithBarcodeProvided() {
        // When
        val exception = assertFailsWith<IllegalArgumentException> {
            EpsonServiceProvider.build(context, printer = null, barcode = mockk())
        }

        // Then
        assertEquals(
            "EpsonServiceProvider requires an ExternalPrinter configuration",
            exception.message,
        )
    }
}
