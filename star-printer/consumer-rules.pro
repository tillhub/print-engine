-keep class com.starmicronics.** { *; }
-dontwarn com.starmicronics.**
-keeppackagenames de.tillhub.printengine.star.**

-keep class de.tillhub.printengine.star.StarPrinterDiscovery { *; }
-keep class de.tillhub.printengine.star.StarServiceProvider { *; }
-keep class com.starmicronics.stario10.** { *; }
-dontwarn com.starmicronics.stario10.**
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**
-keepclassmembers class * {
    void onPrinterFound(com.starmicronics.stario10.StarPrinter);
    void onDiscoveryFinished();
}

-keep class * implements com.starmicronics.stario10.StarDeviceDiscoveryManager$Callback {
    *;
}
-keep class com.starmicronics.** { *; }
-dontwarn com.starmicronics.**
