package de.tillhub.printengine.data

/**
 * State of the [Printer] in case a connection is established.
 */
sealed class PrinterState {
    /** Trying to find supported printer */
    object CheckingForPrinter : PrinterState()

    /** The printer works normally / printer is running */
    object Connected : PrinterState()

    /** Preparing printer / printer found but still initializing */
    object Preparing : PrinterState()

    /** Printer found but operating */
    object Busy : PrinterState()

    /** Printer error */
    sealed class Error : PrinterState() {
        /** Connection to printer has lost */
        object ConnectionLost : Error()

        /** Printer was not detected / does not exist */
        object NotAvailable : Error()

        /** Abnormal communication / printer hardware interface is abnormal */
        object AbnormalCommunication : Error()

        /** Printer is out of paper */
        object OutOfPaper : Error()

        /** Printer is overheating */
        object Overheated : Error()

        /** Printers cover is not closed */
        object CoverNotClosed : Error()

        /** The printers paper cutter is abnormal */
        object PaperCutterAbnormal : Error()

        /** Black mark has not been detected */
        object BlackMarkNotFound : Error()

        /** Failed to upgrade the printer firmware */
        object FirmwareUpgradeFailed : Error()

        /** Paper jammed */
        object PaperJam : Error()

        /** Battery or voltage is too low */
        object VoltageTooLow : Error()

        /** Unknown error state */
        object Unknown : Error()

        /** Print job cannot continue, but could be resumed later. */
        object PrintingUnfinished : Error()

        object Malfunctions : Error()

        /**
         * Pax A920 specific
         */
        sealed class Pax : Error() {
            object FormatPrintDataPacketError : Pax()

            object NotInstalledFontLibrary : Pax()

            object DataPackageTooLong : Pax()
        }

        /**
         * Verifone specific
         */
        sealed class Verifone : Error() {
            object InternalError : Verifone()
        }
    }
}
