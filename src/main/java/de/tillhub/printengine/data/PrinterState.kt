package de.tillhub.printengine.data

/**
 * State of the [Printer] in case a connection is established.
 */
sealed class PrinterState {
    /** The printer works normally / printer is running */
    object Connected : PrinterState()

    /** Preparing printer / printer found but still initializing */
    object Preparing : PrinterState()

    /** Printer was not detected / does not exist */
    object PrinterNotDetected : PrinterState()

    /** Printer found but operating */
    object Busy : PrinterState()

    /** Printer error */
    sealed class Error : PrinterState() {
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

        /** Unknown error state */
        object Unknown : Error()

        /**
         * Pax A920 specific
         */

        object FormatPrintDataPacketError : Error()

        object Malfunctions : Error()

        object VoltageTooLow : Error()

        object PrintingUnfinished : Error()

        object NotInstalledFontLibrary : Error()

        object DataPackageTooLong : Error()
    }
}