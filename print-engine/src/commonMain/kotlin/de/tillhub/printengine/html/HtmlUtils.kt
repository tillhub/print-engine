package de.tillhub.printengine.html

import de.tillhub.printengine.data.ImageBitmap
import de.tillhub.printengine.data.encodeToBase64

object HtmlUtils {

    fun transformToHtml(text: String, includeStyle: Boolean = false): String = if (includeStyle) {
        "<html>$STYLE_TAG<body>$text</body></html>"
    } else {
        "<html><body>$text</body></html>"
    }

    fun monospaceText(text: String, fontSize: Int = FONT_SIZE): String =
        "<pre style=\"font-family: monospace; letter-spacing: 0px; font-size: ${fontSize}px;\">$text</pre>"

    fun singleLineCenteredText(text: String): String =
        "<div style=\"text-align:center;\">$text</div>"

    fun generateImageHtml(image: ImageBitmap): String =
        "<div style=\"text-align:center;\">" +
            "<img src=\"data:image/png;base64,${image.encodeToBase64()}\"/>" +
        "</div>"

    private const val FONT_SIZE = 13
    private const val STYLE_TAG = """
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
    """
}
