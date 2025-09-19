package de.tillhub.printengine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.platform.app.InstrumentationRegistry

object BitmapAssertions {
    fun assertBitmapMatches(
        goldenName: String,
        bitmap: Bitmap,
    ) {
        val golden =
            InstrumentationRegistry
                .getInstrumentation()
                .context.resources.assets
                .open("$goldenName.png")
                .use { BitmapFactory.decodeStream(it) }

        golden.compare(bitmap)
    }

    private fun Bitmap.compare(other: Bitmap) {
        if (this.width != other.width || this.height != other.height) {
            throw AssertionError("Size of screenshot does not match asset file (check device density)")
        }
        // Compare row by row to save memory on device
        val row1 = IntArray(width)
        val row2 = IntArray(width)
        for (column in 0 until height) {
            // Read one row per bitmap and compare
            this.getRow(row1, column)
            other.getRow(row2, column)
            if (!row1.contentEquals(row2)) {
                throw AssertionError("Sizes match but bitmap content has differences")
            }
        }
    }

    private fun Bitmap.getRow(
        pixels: IntArray,
        column: Int,
    ) {
        this.getPixels(pixels, 0, width, 0, column, width, 1)
    }
}
