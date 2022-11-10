plugins {
    id(Dependencies.Plugins.LIBRARY)
}

android {
    compileSdk = ConfigData.targetSdkVersion
    defaultConfig {
        minSdk = ConfigData.minSdkVersion
        targetSdk = ConfigData.targetSdkVersion
        ndk {
            abiFilters.add("armeabi-v7a")
        }
    }
}

dependencies {
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}