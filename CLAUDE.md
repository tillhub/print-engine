# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the Tillhub Print Engine - a Kotlin Multiplatform library that provides a unified interface for printing on Android devices with built-in printers. It supports PAX, Sunmi, Verifone devices and external Star/Epson printers through a plugin-based architecture.

## Commands

### Build and Development
```bash
# Build the entire project
./gradlew build

# Run tests for all modules
./gradlew test

# Run tests for specific module
./gradlew print-engine:test
./gradlew print-plugins:star:test

# Code formatting check (uses ktlint via Spotless)
./gradlew spotlessCheck

# Apply code formatting
./gradlew spotlessApply

# Clean build
./gradlew clean

# Build sample app
./gradlew sample:build
```

### Publishing
```bash
# Publish to Maven Central (requires credentials)
./gradlew publishToMavenCentral
```

## Architecture

### Core Structure
- **print-engine**: Core multiplatform module containing the main API and abstractions
- **print-plugins**: Device-specific implementations organized as separate modules:
  - `pax`: PAX device printer implementation
  - `sunmi`: Sunmi device printer implementation
  - `verifone`: Verifone device printer implementation
  - `star`: Star Micronics external printer implementation
  - `epson`: Epson external printer implementation
- **sample**: Android demo application
- **buildSrc**: Build configuration and shared constants

### Key Components

#### PrintEngine (Singleton)
The main entry point that manages printer discovery, initialization, and provides access to:
- `printer`: The active printer instance
- `barcodeEncoder`: Utility for generating barcode/QR code bitmaps
- `discoverExternalPrinters()`: Flow-based external printer discovery
- `initPrinter()`: Initialize with a specific printer service implementation

#### Plugin Architecture
Each printer plugin implements:
- Device-specific `PrintService` implementations
- Manufacturer detection logic via `PrinterManufacturer`
- Connection and communication protocols
- Print command translation to device APIs

#### Print Commands
The system uses a command pattern with these core types:
- `PrintCommand.Text`: Plain text printing
- `PrintCommand.Image`: Bitmap image printing
- `PrintCommand.Barcode`: Code 128 barcode
- `PrintCommand.QrCode`: QR code printing
- `PrintCommand.FeedPaper`: Paper feeding
- `PrintCommand.CutPaper`: Paper cutting (if supported)

### Data Flow
1. Client creates `PrintJob` with list of `PrintCommand`s
2. `PrintEngine.printer.startPrintJob()` processes commands
3. Commands are translated to device-specific API calls
4. Results returned as `PrinterResult<Unit>`
5. Printer state changes broadcast via `printerState` Flow

### Testing
- Uses Kotest as the primary testing framework
- JUnit 5 platform for test execution
- MockK for mocking in unit tests
- Robolectric for Android-specific testing
- Each module has its own test suite in `src/*/Test/kotlin`

### Configuration
- Build configuration centralized in `buildSrc/src/main/kotlin/Configs.kt`
- Dependencies managed in `gradle/libs.versions.toml`
- Spotless configured for code formatting with ktlint
- Maven publishing configured for JitPack and Maven Central

### Platform-Specific Code
- Common interfaces defined in `commonMain`
- Android implementations in `androidMain`
- iOS framework generation configured for multiplatform support
- Expect/actual classes used for platform abstractions