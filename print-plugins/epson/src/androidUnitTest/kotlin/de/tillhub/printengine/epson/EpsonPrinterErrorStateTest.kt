package de.tillhub.printengine.epson

import com.epson.epos2.Epos2CallbackCode
import com.epson.epos2.Epos2Exception
import com.epson.epos2.printer.Printer
import com.epson.epos2.printer.PrinterStatusInfo
import de.tillhub.printengine.data.PrinterState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class EpsonPrinterErrorStateTest :
    FunSpec({

        context("epsonExceptionToState") {
            test("returns DataError for ERR_PARAM") {
                val e = Epos2Exception(Epos2Exception.ERR_PARAM)
                EpsonPrinterErrorState.epsonExceptionToState(e) shouldBe PrinterState.Error.Epson.DataError
            }

            test("returns NotAvailable for ERR_CONNECT") {
                val e = Epos2Exception(Epos2Exception.ERR_CONNECT)
                EpsonPrinterErrorState.epsonExceptionToState(e) shouldBe PrinterState.Error.NotAvailable
            }

            test("returns ConnectionLost for ERR_TIMEOUT and ERR_DISCONNECT") {
                val timeout = Epos2Exception(Epos2Exception.ERR_TIMEOUT)
                val disconnect = Epos2Exception(Epos2Exception.ERR_DISCONNECT)

                EpsonPrinterErrorState.epsonExceptionToState(timeout) shouldBe PrinterState.Error.ConnectionLost
                EpsonPrinterErrorState.epsonExceptionToState(disconnect) shouldBe PrinterState.Error.ConnectionLost
            }

            test("returns MemoryError for ERR_MEMORY") {
                val e = Epos2Exception(Epos2Exception.ERR_MEMORY)
                EpsonPrinterErrorState.epsonExceptionToState(e) shouldBe PrinterState.Error.Epson.MemoryError
            }

            test("returns InternalError for unknown status") {
                val e = Epos2Exception(9999)
                EpsonPrinterErrorState.epsonExceptionToState(e) shouldBe PrinterState.Error.Epson.InternalError
            }
        }

        context("epsonStatusToState") {
            test("returns Connected for CODE_SUCCESS") {
                val status = mockk<PrinterStatusInfo>(relaxed = true)
                EpsonPrinterErrorState.epsonStatusToState(Epos2CallbackCode.CODE_SUCCESS, status) shouldBe PrinterState.Connected
            }

            test("returns Busy for CODE_PRINTING") {
                val status = mockk<PrinterStatusInfo>(relaxed = true)
                EpsonPrinterErrorState.epsonStatusToState(Epos2CallbackCode.CODE_PRINTING, status) shouldBe PrinterState.Busy
            }

            test("returns ConnectionLost when offline") {
                val status =
                    mockk<PrinterStatusInfo> {
                        every { online } returns Printer.FALSE
                    }
                EpsonPrinterErrorState.epsonStatusToState(999, status) shouldBe PrinterState.Error.ConnectionLost
            }

            test("returns OutOfPaper when paper is empty") {
                val status =
                    mockk<PrinterStatusInfo> {
                        every { online } returns Printer.TRUE
                        every { paper } returns Printer.PAPER_EMPTY
                    }
                EpsonPrinterErrorState.epsonStatusToState(999, status) shouldBe PrinterState.Error.OutOfPaper
            }

            test("returns CoverNotClosed when cover is open") {
                val status =
                    mockk<PrinterStatusInfo> {
                        every { online } returns Printer.TRUE
                        every { paper } returns Printer.PAPER_NEAR_END
                        every { coverOpen } returns Printer.TRUE
                    }
                EpsonPrinterErrorState.epsonStatusToState(999, status) shouldBe PrinterState.Error.CoverNotClosed
            }

            test("returns Malfunctions for MECHANICAL_ERR or UNKNOWN") {
                val status =
                    mockk<PrinterStatusInfo> {
                        every { online } returns Printer.TRUE
                        every { paper } returns Printer.PAPER_NEAR_END
                        every { coverOpen } returns Printer.FALSE
                        every { errorStatus } returns Printer.MECHANICAL_ERR
                    }
                EpsonPrinterErrorState.epsonStatusToState(999, status) shouldBe PrinterState.Error.Malfunctions
            }

            test("returns PaperCutterAbnormal for AUTOCUTTER_ERR") {
                val status =
                    mockk<PrinterStatusInfo> {
                        every { online } returns Printer.TRUE
                        every { paper } returns Printer.PAPER_NEAR_END
                        every { coverOpen } returns Printer.FALSE
                        every { errorStatus } returns Printer.AUTOCUTTER_ERR
                    }
                EpsonPrinterErrorState.epsonStatusToState(999, status) shouldBe PrinterState.Error.PaperCutterAbnormal
            }

            test("returns VoltageTooLow for LOW_VOLTAGE_ERR") {
                val status =
                    mockk<PrinterStatusInfo> {
                        every { online } returns Printer.TRUE
                        every { paper } returns Printer.PAPER_NEAR_END
                        every { coverOpen } returns Printer.FALSE
                        every { errorStatus } returns Printer.UNRECOVER_ERR
                        every { unrecoverError } returns Printer.LOW_VOLTAGE_ERR
                    }
                EpsonPrinterErrorState.epsonStatusToState(999, status) shouldBe PrinterState.Error.VoltageTooLow
            }

            test("returns Overheated for HEAD_OVERHEAT") {
                val status =
                    mockk<PrinterStatusInfo> {
                        every { online } returns Printer.TRUE
                        every { paper } returns Printer.PAPER_NEAR_END
                        every { coverOpen } returns Printer.FALSE
                        every { errorStatus } returns Printer.AUTORECOVER_ERR
                        every { autoRecoverError } returns Printer.HEAD_OVERHEAT
                    }
                EpsonPrinterErrorState.epsonStatusToState(999, status) shouldBe PrinterState.Error.Overheated
            }

            test("returns PaperAbnormal for WRONG_PAPER") {
                val status =
                    mockk<PrinterStatusInfo> {
                        every { online } returns Printer.TRUE
                        every { paper } returns Printer.PAPER_NEAR_END
                        every { coverOpen } returns Printer.FALSE
                        every { errorStatus } returns Printer.AUTORECOVER_ERR
                        every { autoRecoverError } returns Printer.WRONG_PAPER
                    }
                EpsonPrinterErrorState.epsonStatusToState(999, status) shouldBe PrinterState.Error.PaperAbnormal
            }

            test("returns CoverNotClosed for COVER_OPEN") {
                val status =
                    mockk<PrinterStatusInfo> {
                        every { online } returns Printer.TRUE
                        every { paper } returns Printer.PAPER_NEAR_END
                        every { coverOpen } returns Printer.FALSE
                        every { errorStatus } returns Printer.AUTORECOVER_ERR
                        every { autoRecoverError } returns Printer.COVER_OPEN
                    }
                EpsonPrinterErrorState.epsonStatusToState(999, status) shouldBe PrinterState.Error.CoverNotClosed
            }

            test("returns VoltageTooLow when battery is level 0") {
                val status =
                    mockk<PrinterStatusInfo> {
                        every { online } returns Printer.TRUE
                        every { paper } returns Printer.PAPER_NEAR_END
                        every { coverOpen } returns Printer.FALSE
                        every { errorStatus } returns 0
                        every { batteryLevel } returns Printer.BATTERY_LEVEL_0
                    }
                EpsonPrinterErrorState.epsonStatusToState(999, status) shouldBe PrinterState.Error.VoltageTooLow
            }

            test("returns Connected if no errors and all other conditions are false") {
                val status =
                    mockk<PrinterStatusInfo> {
                        every { online } returns Printer.TRUE
                        every { paper } returns Printer.PAPER_NEAR_END
                        every { coverOpen } returns Printer.FALSE
                        every { errorStatus } returns 0
                        every { batteryLevel } returns 2
                    }
                EpsonPrinterErrorState.epsonStatusToState(999, status) shouldBe PrinterState.Connected
            }
        }
    })
