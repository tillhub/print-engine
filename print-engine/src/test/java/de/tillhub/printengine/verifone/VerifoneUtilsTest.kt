package de.tillhub.printengine.verifone

import android.graphics.Bitmap
import de.tillhub.printengine.HtmlUtils
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.robolectric.RobolectricTest
import io.kotest.matchers.shouldBe

@RobolectricTest
internal class VerifoneUtilsTest : FunSpec({

    test("transformToHtml") {
        val result = HtmlUtils.transformToHtml("Receipt text")
        result shouldBe "<html><body>Receipt text</body></html>"
    }

    test("monospaceText") {
        val result = HtmlUtils.monospaceText("Line text")
        result shouldBe "<pre style=\"font-family: monospace; " +
                "letter-spacing: 0px; font-size: 20px;\">Line text</pre>"
    }

    test("singleLineCenteredText") {
        val result = HtmlUtils.singleLineCenteredText("Single line text")
        result shouldBe "<div style=\"text-align:center;\">Single line text</div>"
    }

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
