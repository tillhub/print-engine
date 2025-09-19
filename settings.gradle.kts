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

rootProject.name = "Tillhub_Print_Engine"
include(":print-engine")
include(":print-plugins:star")
include(":print-plugins:epson")
include(":print-plugins:pax")
include(":print-plugins:sunmi")
include(":print-plugins:verifone")
include(":sample")