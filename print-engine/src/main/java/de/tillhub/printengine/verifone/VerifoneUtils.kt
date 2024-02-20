package de.tillhub.printengine.verifone

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
}