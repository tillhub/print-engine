## StarIO10 / StarXpand SDK - keep rules for 1.11.x
## Keep Star classes and referenced descriptor classes
-keep,includedescriptorclasses class com.starmicronics.** { *; }

## Preserve important attributes used by reflection/inner classes
-keepattributes Exceptions,InnerClasses,EnclosingMethod,Signature,SourceFile,LineNumberTable,
    RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,RuntimeVisibleParameterAnnotations,
    RuntimeInvisibleParameterAnnotations,AnnotationDefault

## Keep native method signatures intact
-keepclasseswithmembernames class * { native <methods>; }

## Suppress warnings from Star libraries
-dontwarn com.starmicronics.**