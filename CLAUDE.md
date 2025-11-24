# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Tillhub Print Engine is an Android library that provides a unified interface for printing to various printer hardware. It abstracts away device-specific implementations and provides a clean API for printing receipts with text, images, barcodes, and QR codes.

**Key concepts:**
- **Plugin architecture**: Each printer manufacturer (PAX, Sunmi, Verifone, Star, Epson) has its own plugin module in `print-plugins/`
- **Automatic device detection**: The library detects the device manufacturer and selects the appropriate printer implementation
- **Fallback to EmulatedPrinter**: On unsupported devices/emulators, prints to Logcat for development
- **External printer support**: Star and Epson printers can be discovered and connected via Bluetooth/USB

## Build Commands

### Build the library
```bash
./gradlew :print-engine:build
```

### Build all modules
```bash
./gradlew build
```

### Run tests
```bash
# Run all tests
./gradlew test

# Run tests for specific module
./gradlew :print-engine:test
./gradlew :print-plugins:star:test
```

### Code quality
```bash
# Run detekt linting
./gradlew detekt

# Generate detekt baseline
./gradlew detektBaseline
```

### Build sample app
```bash
./gradlew :sample:assembleDebug
```

### Clean build
```bash
./gradlew clean
```

## Project Structure

### Core module: `print-engine/`
The main library module that provides the public API and core abstractions.

**Key files:**
- `PrintEngine.kt` - Singleton entry point for the library. Provides access to `Printer`, `BarcodeEncoder`, and external printer discovery
- `Printer.kt` - Main interface for printing operations. Exposes `startPrintJob()` and `observePrinterState()`
- `PrintService.kt` - Abstract base class that each printer plugin extends
- `PrinterContainer.kt` - Internal container that holds the active printer implementation and delegates to it
- `PrinterController.kt` - Low-level controller interface that implementations use to execute print commands
- `data/PrintCommand.kt` - Sealed class hierarchy for print commands (Text, Image, Barcode, QrCode, RawData, FeedPaper, CutPaper)
- `data/PrintJob.kt` - Container for a list of PrintCommands to be executed sequentially
- `data/PrinterManufacturer.kt` - Enum detecting device manufacturer from `android.os.Build.MANUFACTURER`

### Plugin modules: `print-plugins/`
Each plugin implements printer-specific logic by extending `PrintService` and implementing `PrinterServiceProvider`.

**Plugin modules:**
- `pax/` - PAX terminal built-in printers (A920, A920 Pro)
- `sunmi/` - Sunmi terminal built-in printers (V2, V2 PRO, V2s)
- `verifone/` - Verifone terminal built-in printers
- `star/` - Star Micronics external printers (via StarXpand SDK) with Bluetooth/USB discovery
- `epson/` - Epson external printers (via ePOS2 SDK) with Bluetooth/USB discovery

**Plugin architecture pattern:**
1. Each plugin has a `*ServiceProvider` (implements `PrinterServiceProvider`) that builds the `PrintService`
2. Each plugin has a `*PrintService` (extends `PrintService`) that manages connection and printing
3. Each plugin has a `*PrinterController` (implements `PrinterController`) that executes low-level commands
4. Star and Epson plugins provide `*PrinterDiscovery` (implements `PrinterDiscovery`) for device discovery

### Sample app: `sample/`
Example Android app demonstrating library usage.

### Build configuration: `buildSrc/`
Contains shared build configuration in `Configs.kt`:
- Application ID: `de.tillhub.printengine`
- Compile SDK: 34
- Min SDK: 23
- Java/JVM version: 17

## Architecture Flow

### For built-in printers (PAX, Sunmi, Verifone):
1. App calls `PrintEngine.getInstance(context)` to get singleton
2. Access `printEngine.printer` - automatically detects manufacturer and creates appropriate service
3. Call `printer.startPrintJob(printJob)` with a list of PrintCommands
4. Commands are executed sequentially by the device-specific PrinterController

### For external printers (Star, Epson):
1. App calls `printEngine.discoverExternalPrinters(StarPrinterDiscovery)` to discover devices
2. User selects a printer from discovery results
3. App builds service: `val service = printer.manufacturer.build(context, printer)`
4. Initialize: `printEngine.initPrinter(service)`
5. Print: `printEngine.printer.startPrintJob(printJob)`

### Print job execution:
- A `PrintJob` contains a list of `PrintCommand` objects
- Commands are executed sequentially by the active `PrintService`
- Each `PrintService` delegates to its `PrinterController` for low-level operations
- Printer state changes are emitted via `printer.observePrinterState()` flow
- Results are returned as `PrinterResult.Success` or `PrinterResult.Error`

## Testing

- Tests use **Kotest** framework with JUnit 5 runner
- **Robolectric** is used for Android framework testing
- **MockK** for mocking
- Test files are located in `src/test/java/` directories within each module

**Running a single test:**
```bash
# Run a specific test class
./gradlew :print-engine:test --tests "de.tillhub.printengine.PrinterImplTest"

# Run a specific test method
./gradlew :print-engine:test --tests "de.tillhub.printengine.PrinterImplTest.should succeed when printing"
```

## Dependencies

Key dependencies defined in `gradle/libs.versions.toml`:
- **ZXing** (`google-zxing`) - Used by `BarcodeEncoderImpl` to generate barcode/QR code bitmaps
- **Timber** - Logging (used in `EmulatedPrinter`)
- **Sunmi Printer Library** (`sunmi-printer`) - Sunmi SDK
- **Star Printer** (`stario10`) - Star Micronics SDK (version downgraded to 1.0.1 to avoid instrumentation crashes)
- Epson ePOS2 SDK is included as binary dependency

## Important Implementation Details

### Barcode encoding
Some printer implementations don't support native barcode printing. The library provides `BarcodeEncoder` that converts barcode content to bitmap images using ZXing, which can then be printed as images.

### PrinterContainer pattern
`PrintEngine.printer` returns a `PrinterContainer` that acts as a proxy. It starts with an `EmulatedPrinter` and switches to the real implementation when `initPrinter()` is called. This allows the printer reference to remain stable while the underlying implementation changes.

### Print analytics
`PrintAnalytics` interface can be attached to log print operations:
```kotlin
printEngine.setAnalytics(object : PrintAnalytics {
    override fun logPrintReceipt(receiptText: String) { /* ... */ }
    override fun logErrorPrintReceipt(message: String) { /* ... */ }
})
```
Must be set before accessing `printEngine.printer` to take effect.

### Packaging requirements
Apps using this library must enable legacy JNI packaging in their `build.gradle`:
```groovy
packaging {
    jniLibs {
        useLegacyPackaging = true
    }
}
```

## Publishing

The library is published to JitPack. Each module has Maven publishing configured:
- Core: `de.tillhub.printengine:core:VERSION`
- Plugins: `de.tillhub.printengine:star-printer:VERSION`, etc.

Version is defined in `buildSrc/src/main/kotlin/Configs.kt` as `VERSION_CODE`.
