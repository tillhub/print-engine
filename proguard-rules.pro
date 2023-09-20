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

-keep class de.tillhub.printengine.analytics.PrintAnalytics {
    *;
}
-keep class de.tillhub.printengine.Printer {
    *;
}
-keep class de.tillhub.printengine.Printer$Companion {
    *;
}
-keep class de.tillhub.printengine.pax.barcode.BarcodeEncoder {
    *;
}
-keep class de.tillhub.printengine.pax.barcode.BarcodeType {
    *;
}
-keep class de.tillhub.printengine.PrintEngine {
    *;
}
-keep class de.tillhub.printengine.data.** {
    *;
}
-keep public class de.tillhub.printengine.helper.SingletonHolder {
    *;
}

-keep class com.pax.** {
    *;
}
-keep public class com.sunmi.** {
    public protected *;
}
