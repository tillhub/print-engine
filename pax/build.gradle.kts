plugins {
    id(Dependencies.Plugins.LIBRARY)
}

repositories {
    google()
    mavenCentral()
}

android {
    compileSdk = ConfigData.targetSdkVersion
    defaultConfig {
        ndk {
            abiFilters.add("armeabi-v7a")
        }
    }
}

dependencies {
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}