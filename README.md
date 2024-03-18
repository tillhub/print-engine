[![](https://jitpack.io/v/tillhub/print-engine.svg)](https://jitpack.io/#tillhub/print-engine)
[![API](https://img.shields.io/badge/API-21%2B-green.svg?style=flat)](https://android-arsenal.com/api?level-11)
# Print Engine

This library combines various printer implementations into a single, easy-to-use interface.

# How to setup

**Step 1.** Add the JitPack repository to your `settings.gradle` file:

```groovy
dependencyResolutionManagement {
    repositories {
        ...
        mavenCentral()
		maven { url 'https://jitpack.io' }
    }
}
```

**Step 2.** Add the dependency to your app `build.gradle`:
```groovy
dependencies {
    implementation 'com.github.tillhub:print-engine:x.x.x'
}
```

**Step 3.** Enable JNI legacy packaging
```groovy
packaging {
    jniLibs {
        useLegacyPackaging = true
    }
}
```

# Usage

This SDK offers supports 2 types printing devices:
* PAX devices with a build in printer (A920, A920 pro, etc.)
* Sunmi devices with a build in printer (V2, V2 PRO, V2s, etc.)
* Verifone devices with a build in printer

For devices without printer support the SDK defaults to an `EmulatedPrinter` implementation that prints into Logcat, this way it is easy to develop on emulators and unsupported devices.
The SDK will automatically select the correct implementation based on the device its running on.

### 1. Printer:

*  `Singleton Access`: Obtain a singleton reference to the `PrinterEngine` instance.
*  `Initialization`: Create a per Context Printer instance. SDK will automatically selects the appropriate printer based on the device manufacturer (Sunmi, Pax, or emulated).
*  `Initiate print`: Call `printer.startPrintJob(printJob)` to initiate printing. Pass appropriate PrintJob that should be printed. When print is finished it returns `PrinterResult`. 
*  `Handle printer state`: Subscribe to the `printer.observePrinterState()` flow to receive `PrinterState` objects that inform about the state of printer.

```kotlin

override fun onCreate(savedInstanceState: Bundle?) {
    // ...
    
    val printEngine = PrintEngine.getInstance(context)
    val printer = printEngine.printer
    
    printButton.setOnClickListener {
        lifecycleScope.launch {
            val result = printer.startPrintJob(printJob)
            when (result) {
                is PrinterResult.Success -> {
                    // Handle success
                }
                is PrinterResult.Error -> {
                    // Handle failure
                }
            }
        }
    }

    lifecycleScope.launch {
        // Observing printer state of printer
        printer.observePrinterState().collect { printerState ->
            // Handle printerState result
        }
    }
}

companion object {
    private val printJob = PrintJob(listOf(
        PrintCommand.Text("This is a line"),
        PrintCommand.Text("This is a another line"),
        PrintCommand.Text("-------"),
        PrintCommand.Text("Barcode:"),
        PrintCommand.Barcode("123ABC"),
        PrintCommand.Text("QR code:"),
        PrintCommand.QrCode("123ABC"),
        PrintCommand.FeedPaper,
    ))
}
```

#### 1.1 PrintJob

A `PrintJob` is defined as a set of `PrintCommand` objects, the commands will be executed sequentially, with that a receipt can be build.
The `PrintJob` object offers 2 convenience values:
* `isNotEmpty` gives the information if a `PrintJob` is empty or not
* `description` gives a string representation of the build receipt

List of commands:
```kotlin
PrintCommand.Text(val text: String)
PrintCommand.Image(val image: Bitmap)
PrintCommand.Barcode(val barcode: String)
PrintCommand.QrCode(val code: String)
PrintCommand.RawData(val data: RawPrinterData)
    /**
     *  Due to the distance between the paper hatch and the print head,
     *  the paper needs to be fed out automatically
     *  But if the Api does not support it, it will be replaced by printing three lines
     */
PrintCommand.FeedPaper
    /**
     *  Printer cuts paper and throws exception on machines without a cutter
     */
PrintCommand.CutPaper
```

### 2. PrintAnalytics:

```kotlin
interface PrintAnalytics {
    fun logPrintReceipt(receiptText: String)
    fun logErrorPrintReceipt(message: String)
}
```

The PrinterEngine SDK offers a `PrintAnalytics` interface that can be attached to the `PrinterEngine` and will call log methods for printed receipts and errors.
This can be used to for analytics and logging purposes.
The interface implementation has to be attached to the engine instance before getting the printer instance for it to work.

PrintAnalytics usage:
```kotlin

override fun onCreate(savedInstanceState: Bundle?) {
    // ...
    
    val printEngine = PrintEngine.getInstance(context)

    // attaching the interface implementation
    printEngine.setAnalytics(object : PrintAnalytics {
        override fun logPrintReceipt(receiptText: String) {
            // Handle printed receipt text
        }

        override fun logErrorPrintReceipt(message: String) {
            // Handle printing error
        }
    })
    
    val printer = printEngine.printer
    
}
```

### 3. BarcodeEncoder

Because some printing implementations don't support printing barcodes as is, the `BarcodeEncoder` implementation was made that generates a bitmap of a Code 128 barcode or QR code, that is then printed.
The `PrinterEngine` offers an instance of this interface that can be used in the host app for convenience.
In case of an error at time of bitmap generation an `null` value is returned.

```kotlin

override fun onCreate(savedInstanceState: Bundle?) {
    // ...
    
    val printEngine = PrintEngine.getInstance(context)

    val barcodeEncoder = printEngine.barcodeEncoder
    
    val qrCode: Bitmap? = barcodeEncoder.encodeAsBitmap(
        content = "barcode_content",
        type = BarcodeType.QR_CODE,
        imgWidth = 500,
        imgHeight = 500
    )

    val code128: Bitmap? = barcodeEncoder.encodeAsBitmap(
        content = "barcode_content",
        type = BarcodeType.CODE_128,
        imgWidth = 500,
        imgHeight = 250
    )
}
```

## Credits

- [Đorđe Hrnjez](https://github.com/djordjeh)
- [Martin Širok](https://github.com/SloInfinity)
- [Chandrashekar Allam](https://github.com/shekar-allam)

## License

```licence
MIT License

Copyright (c) 2024 Tillhub GmbH

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
