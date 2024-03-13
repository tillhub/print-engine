package de.tillhub.printengine.verifone

import android.graphics.Bitmap
import android.util.Base64
import androidx.annotation.VisibleForTesting
import java.io.ByteArrayOutputStream

object VerifoneUtils {

    fun transformToHtml(text: String): String =
        "<html><body>$text</body></html>"

    fun monospaceText(text: String): String =
        "<pre style=\"font-family: monospace; letter-spacing: 0px; font-size: 20px;\">$text</pre>"

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

    const val FEED_PAPER: String = "<br /><br /><br /><br /><br />"
    private const val PNG_QUALITY = 100
}