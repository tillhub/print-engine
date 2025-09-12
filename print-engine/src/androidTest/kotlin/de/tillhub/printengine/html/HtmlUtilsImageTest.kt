package de.tillhub.printengine.html

import android.graphics.Bitmap
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.robolectric.RobolectricTest
import io.kotest.matchers.shouldBe

@RobolectricTest
internal class HtmlUtilsImageTest : FunSpec({
    test("generateImageHtml") {
        val bitmap: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val result = HtmlUtils.generateImageHtml(bitmap)
        bitmap.recycle()

        result shouldBe "<div style=\"text-align:center;\">" +
                "<img src=\"data:image/png;base64," +
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAEElEQVR4AQEFAPr/AAAAAAAABQAB\n" +
                "ZHiVOAAAAABJRU5ErkJggg==\n\"/></div>"
    }
})
