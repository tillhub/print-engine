package de.tillhub.printengine.data

import de.tillhub.printengine.helpers.HashHelper
import de.tillhub.printengine.data.ImageBitmap

sealed class PrintCommand {
    class Text(val text: String) : PrintCommand() {
        override fun toString() = "PrintCommand.Text(" +
            "text=$text" +
            ")"

        override fun equals(other: Any?) = other is Text &&
            text == other.text

        override fun hashCode() = HashHelper.hash(text)
    }
    class Image(val image: ImageBitmap) : PrintCommand() {
        override fun toString() = "PrintCommand.Image(" +
            "image=$image" +
            ")"

        override fun equals(other: Any?) = other is Image &&
            image == other.image

        override fun hashCode() = HashHelper.hash(image)
    }
    class Barcode(val barcode: String) : PrintCommand() {
        override fun toString() = "PrintCommand.Text(" +
            "barcode=$barcode" +
            ")"

        override fun equals(other: Any?) = other is Barcode &&
            barcode == other.barcode

        override fun hashCode() = HashHelper.hash(barcode)
    }
    class QrCode(val code: String) : PrintCommand() {
        override fun toString() = "PrintCommand.QrCode(" +
            "code=$code" +
            ")"

        override fun equals(other: Any?) = other is QrCode &&
            code == other.code

        override fun hashCode() = HashHelper.hash(code)
    }
    class RawData(val data: RawPrinterData) : PrintCommand() {
        override fun toString() = "PrintCommand.RawData(" +
            "data=$data" +
            ")"

        override fun equals(other: Any?) = other is RawData &&
            data == other.data

        override fun hashCode() = HashHelper.hash(data)
    }
    /**
     *  Due to the distance between the paper hatch and the print head,
     *  the paper needs to be fed out automatically
     *  But if the Api does not support it, it will be replaced by printing three lines
     */
    data object FeedPaper : PrintCommand()
    /**
     *  Printer cuts paper and throws exception on machines without a cutter
     */
    data object CutPaper : PrintCommand()
}
