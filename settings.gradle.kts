import java.net.URI

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = URI.create("https://jitpack.io")
        }
    }
}

rootProject.name = "Tillhub Print Engine"
include(":print-engine")
include(":star-printer")
include(":epson-printer")
include(":pax-printer")
include(":sunmi-printer")
include(":verifone-printer")
include(":sample")