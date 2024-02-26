package de.tillhub.printengine.verifone

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

object VerifoneUtils {

    fun transformToHtml(text: String): String =
        "<html>" +
            "<body>" +
                """<pre style="font-family: monospace; letter-spacing: 0px; font-size: 20px;">""" + "\n" +
                text +
                "\n</pre>" +
            "</body>" +
        "</html>"

    fun singleLineCenteredText(text: String): String =
        "<table style='width:100%' border=\"0\">" +
            "<tr><td style=\"text-align: center;\">$text</td></tr>" +
        "</table>"

    fun transformToHtml(image: Bitmap): String =
        "<tr><td style=\"text-align: center;\">" +
            "<img src=\"data:image/png;base64, ${ encodeImage(image) }\" alt=\"Red dot\" />" +
        "</td></tr>"

    private fun encodeImage(image: Bitmap): String = ByteArrayOutputStream().let { stream ->
        image.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)
        val b = stream.toByteArray()
        Base64.encodeToString(b, Base64.DEFAULT)
    }

    private const val JPEG_QUALITY = 100
}