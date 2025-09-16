package de.tillhub.printengine.html

import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlUtilsTest {
    @Test
    fun transformToHtml() {
        val result = HtmlUtils.transformToHtml("Receipt text")
        assertEquals("<html><body>Receipt text</body></html>", result)
    }

    @Test
    fun `transformToHtml with style`() {
        val result = HtmlUtils.transformToHtml("Receipt text", includeStyle = true)

        val expected = """<html>
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

        assertEquals(expected, result)
    }

    @Test
    fun `monospaceText`() {
        val result = HtmlUtils.monospaceText("Line text")
        assertEquals(
            "<pre style=\"font-family: monospace; " +
                "letter-spacing: 0px; font-size: 13px;\">Line text</pre>",
            result,
        )
    }

    @Test
    fun `monospaceText with custom size`() {
        val result = HtmlUtils.monospaceText("Line text", fontSize = 15)
        assertEquals(
            "<pre style=\"font-family: monospace; " +
                "letter-spacing: 0px; font-size: 15px;\">Line text</pre>",
            result,
        )
    }

    @Test
    fun `singleLineCenteredText`() {
        val result = HtmlUtils.singleLineCenteredText("Single line text")
        assertEquals("<div style=\"text-align:center;\">Single line text</div>", result)
    }
}
