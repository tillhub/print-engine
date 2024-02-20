plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.detekt)
    id("maven-publish")
}

android {
    namespace = "de.tillhub.printengine"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        ndk {
            abiFilters.add("armeabi-v7a")
        }
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_17.toString()
            freeCompilerArgs = listOf(
                "-Xstring-concat=inline"
            )
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
    implementation(libs.bundles.core)
    coreLibraryDesugaring(libs.android.desugarJdkLibs)
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // Pax Dependencies
    implementation(libs.google.zxing)

    // Sunmi Dependencies
    implementation(libs.sunmi.printer)

    // Utils
    implementation(libs.timber)
    detektPlugins(libs.detekt.formatting)

    // Unit tests
    testImplementation(libs.bundles.testing)
    testImplementation(libs.bundles.robolectric)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("print-engine") {
                groupId = "de.tillhub.printengine"
                artifactId = "print-engine"
                version = "1.7.0"

                from(components.getByName("release"))
            }
        }
    }
}
