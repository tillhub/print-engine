package de.tillhub.printengine.html

import android.graphics.Bitmap
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.robolectric.RobolectricTest
import io.kotest.matchers.shouldBe

@RobolectricTest
internal class HtmlUtilsTest : FunSpec({

    test("transformToHtml") {
        val result = HtmlUtils.transformToHtml("Receipt text")
        result shouldBe "<html><body>Receipt text</body></html>"
    }

    test("transformToHtml with style") {
        val result = HtmlUtils.transformToHtml("Receipt text", includeStyle = true)
        result shouldBe """<html>
        <style type="text/css">
            @page {
                margin-left: 0.55cm;
                margin-right: 0.55cm;
                margin-top: 0.55cm
                margin-bottom: 0px;
            }
            pre {
                padding: 0;
                margin: 0;
            }
            div {
                padding: 0;
                margin: 0;
            }
            img {
                max-width: 100%;
                max-height: 100%;
            }
        </style>
    <body>Receipt text</body></html>"""
    }

    test("monospaceText") {
        val result = HtmlUtils.monospaceText("Line text")
        result shouldBe "<pre style=\"font-family: monospace; " +
                "letter-spacing: 0px; font-size: 20px;\">Line text</pre>"
    }

    test("monospaceText with custom size") {
        val result = HtmlUtils.monospaceText("Line text", fontSize = 15)
        result shouldBe "<pre style=\"font-family: monospace; " +
                "letter-spacing: 0px; font-size: 15px;\">Line text</pre>"
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
