package de.tillhub.printengine.epson

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.tillhub.printengine.data.ExternalPrinter
import io.mockk.mockk
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EpsonManufacturerTest {

    @Test
    fun build_shouldReturnEpsonPrintService() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val printer = mockk<ExternalPrinter>(relaxed = true)

        val result = EpsonManufacturer.build(context, printer)

        assertTrue(result is EpsonPrintService)
    }
}