package de.tillhub.printengine.verifone

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class VerifoneUtilsTest : FunSpec({

    test("transformToHtml") {
       VerifoneUtils.transformToHtml(
               "--------------------------------\n" +
               "             KOPIE              \n" +
               "--------------------------------\n" +
               "\n" +
               "    H-Ä-N-D-L-E-R-B-E-L-E-G     \n" +
               "                                \n" +
               "     Custom Receipt Header      \n" +
               "--------------------------------\n" +
               "Ausgabenbeleg:                 1\n" +
               "Kassierer:          Đorđe Hrnjez\n" +
               "Berechtigungen:     Đorđe Hrnjez\n" +
               "Datum und Zeit:   12.10.23 16:55\n" +
               "Filiale:                       1\n" +
               "Kasse:                    567890\n" +
               "--------------------------------\n" +
               "                                \n" +
               "8505 Einzahlung          20,00€\n" +
               "                                \n" +
               "--------------------------------") shouldBe
               "<html>" +
               "<body>" +
               """<pre style="font-family: monospace; letter-spacing: 0px; font-size: 20px;">""" + "\n" +
               "--------------------------------\n" +
               "             KOPIE              \n" +
               "--------------------------------\n" +
               "\n" +
               "    H-Ä-N-D-L-E-R-B-E-L-E-G     \n" +
               "                                \n" +
               "     Custom Receipt Header      \n" +
               "--------------------------------\n" +
               "Ausgabenbeleg:                 1\n" +
               "Kassierer:          Đorđe Hrnjez\n" +
               "Berechtigungen:     Đorđe Hrnjez\n" +
               "Datum und Zeit:   12.10.23 16:55\n" +
               "Filiale:                       1\n" +
               "Kasse:                    567890\n" +
               "--------------------------------\n" +
               "                                \n" +
               "8505 Einzahlung          20,00€\n" +
               "                                \n" +
               "--------------------------------" +
               "\n</pre>" +
               "</body>" +
               "</html>"
    }

    test("singleLineCenteredText") {
     VerifoneUtils.singleLineCenteredText("SINGLE_LINE") shouldBe
             "<table style='width:100%' border=\"0\">" +
             "<tr><td style=\"text-align: center;\">SINGLE_LINE</td></tr>" +
             "</table>"
    }
})
