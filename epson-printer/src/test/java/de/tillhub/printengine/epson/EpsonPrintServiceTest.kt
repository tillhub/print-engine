package de.tillhub.printengine.epson

/**
 * Test class for EpsonPrintService.
 *
 * Note: Unit tests for this class cannot be written because the Epson epos2 library is not found in
 * java.library.path in the test environment, causing runtime errors when attempting to load the native
 * library. Additionally, Android instrumentation tests cannot be implemented due to a known issue with
 * MockK (https://github.com/mockk/mockk/issues/1300), which prevents proper mocking of dependencies in
 * the Android test environment.
 */
class EpsonPrintServiceTest
