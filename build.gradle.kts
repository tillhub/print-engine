plugins {
    kotlin(Dependencies.KotlinPlugins.ANDROID)
    kotlin(Dependencies.KotlinPlugins.KAPT)
    id(Dependencies.Plugins.LIBRARY)
    id(Dependencies.Plugins.DETEKT) version Versions.Plugins.DETEKT
    id(Dependencies.Plugins.PUBLISH)
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
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

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = ConfigData.JAVA_VERSION
        targetCompatibility = ConfigData.JAVA_VERSION
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = ConfigData.JVM_TARGET
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    // Core Dependencies
    implementDependencyGroup(Dependencies.Groups.CORE)
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // Pax Dependencies
    implementation(Dependencies.Google.ZXING)

    // Sunmi Dependencies
    implementation(Dependencies.Sunmi.PRINTER)

    // Utils
    implementation(Dependencies.Tools.TIMBER)

    // Unit tests
    implementDependencyGroup(Dependencies.Groups.TEST_LIBRARIES)
    implementDependencyGroup(Dependencies.Groups.TEST_ROBOLECTRIC)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>(ConfigData.artifactId) {
                groupId = ConfigData.applicationId
                artifactId = ConfigData.artifactId
                version = ConfigData.versionName

                from(components.getByName("release"))
            }
        }
    }
}
