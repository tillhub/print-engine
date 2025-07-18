package de.tillhub.printengine.html

import android.graphics.Bitmap
import android.util.Base64
import androidx.annotation.VisibleForTesting
import java.io.ByteArrayOutputStream

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

    fun generateImageHtml(image: Bitmap): String =
        "<div style=\"text-align:center;\">" +
            "<img src=\"data:image/png;base64,${encodeToBase64(image)}\"/>" +
        "</div>"

    @VisibleForTesting
    fun encodeToBase64(image: Bitmap): String = ByteArrayOutputStream().let { stream ->
        image.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, stream)
        val b = stream.toByteArray()
        Base64.encodeToString(b, Base64.DEFAULT)
    }

    private const val PNG_QUALITY = 100
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
