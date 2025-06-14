# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keeppackagenames de.tillhub.printengine.**

-keep class de.tillhub.printengine.Printer { *; }
-keep class de.tillhub.printengine.Printer$Companion { *; }
-keep class de.tillhub.printengine.PrintService { *; }
-keep class de.tillhub.printengine.PrinterController { *; }
-keep class de.tillhub.printengine.external.PrinterDiscovery { *; }
-keep class de.tillhub.printengine.external.PrinterServiceProvider { *; }
-keep class de.tillhub.printengine.PrintEngine { *; }
-keep class de.tillhub.printengine.PrintEngine$Companion { *; }
-keep class de.tillhub.printengine.helper.SingletonHolder { *; }
-keep class de.tillhub.printengine.analytics.PrintAnalytics { *; }
-keep class de.tillhub.printengine.data.** { *; }
-keep class de.tillhub.printengine.barcode.BarcodeEncoder {  *; }
-keep class de.tillhub.printengine.barcode.BarcodeType { *; }
-keep class de.tillhub.printengine.data.ConnectionType { *; }

-keep class com.pax.** { *; }
-keep public class com.sunmi.** {
    public protected *;
}
-keep class com.verifone.peripherals.** { *; }

-dontwarn com.verifone.peripherals.CashDrawer
-dontwarn com.verifone.peripherals.Scanner
-dontwarn com.verifone.utilities.BaseParcel$ParcelCreator
-dontwarn com.verifone.utilities.BaseParcel