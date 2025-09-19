package de.tillhub.printengine.data

/**
 * State of the [Printer] in case a connection is established.
 */
sealed class PrinterState {
    /** Trying to find supported printer */
    data object CheckingForPrinter : PrinterState()

    /** The printer works normally / printer is running */
    data object Connected : PrinterState()

    /** Preparing printer / printer found but still initializing */
    data object Preparing : PrinterState()

    /** Printer found but operating */
    data object Busy : PrinterState()

    /** Printer error */
    sealed class Error : PrinterState() {
        /** Connection to printer has lost */
        data object ConnectionLost : Error()

        /** Printer was not detected / does not exist */
        data object NotAvailable : Error()

        /** Abnormal communication / printer hardware interface is abnormal */
        data object AbnormalCommunication : Error()

        /** Printer is out of paper */
        data object OutOfPaper : Error()

        /** Printer is overheating */
        data object Overheated : Error()

        /** Printers cover is not closed */
        data object CoverNotClosed : Error()

        /** The printers paper cutter is abnormal */
        data object PaperCutterAbnormal : Error()

        /** The printers paper is abnormal */
        data object PaperAbnormal : Error()

        /** Black mark has not been detected */
        data object BlackMarkNotFound : Error()

        /** Failed to upgrade the printer firmware */
        data object FirmwareUpgradeFailed : Error()

        /** Paper jammed */
        data object PaperJam : Error()

        /** Battery or voltage is too low */
        data object VoltageTooLow : Error()

        /** Unknown error state */
        data object Unknown : Error()

        /** Print job cannot continue, but could be resumed later. */
        data object PrintingUnfinished : Error()

        data object Malfunctions : Error()

        /**
         * Pax A920 specific
         */
        sealed class Pax : Error() {
            data object FormatPrintDataPacketError : Pax()

            data object NotInstalledFontLibrary : Pax()

            data object DataPackageTooLong : Pax()
        }

        /**
         * Verifone specific
         */
        sealed class Verifone : Error() {
            data object InternalError : Verifone()
        }

        /**
         * Epson specific
         */
        sealed class Epson : Error() {
            data object MemoryError : Epson()

            data object DataError : Epson()

            data object InternalError : Epson()
        }
    }
}
