import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.mokkery)
    id("maven-publish")
}

kotlin {
    compilerOptions {
        // removes warnings for expect/actual classes
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidTarget {
        // Keep JVM target consistent with Java 17
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }

        dependencies {
            // Core Dependencies
            implementation(libs.androidx.core)
            coreLibraryDesugaring(libs.android.desugarJdkLibs)

            implementation(libs.google.zxing)

            // Unit tests
            testImplementation(libs.bundles.testing)
            testImplementation(libs.bundles.robolectric)
        }
    }

    val xcfName = "print-engine"
    iosX64 { binaries.framework { baseName = xcfName } }
    iosArm64 { binaries.framework { baseName = xcfName } }
    iosSimulatorArm64 { binaries.framework { baseName = xcfName } }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlin.coroutines)

                // Utils
                implementation(libs.kermit)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))

                implementation(libs.kotlin.coroutines.test)
            }
        }

        val androidMain by getting {
            // Reuse existing Android sources under src/main/kotlin to avoid moving files now
            kotlin.srcDirs("src/androidMain/kotlin")
            dependencies {
                implementation(libs.kotlin.coroutines.android)
            }
        }
        val androidUnitTest by getting {
            // Reuse existing Android unit tests under src/test/kotlin
            kotlin.srcDirs("src/androidTest/kotlin")
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

android {
    namespace = Configs.APPLICATION_ID
    compileSdk = Configs.COMPILE_SDK

    defaultConfig {
        minSdk = Configs.MIN_SDK
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            consumerProguardFiles(
                "consumer-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = Configs.JAVA_VERSION
        targetCompatibility = Configs.JAVA_VERSION
        isCoreLibraryDesugaringEnabled = true
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

// afterEvaluate {
//    publishing {
//        publications {
//            create<MavenPublication>("release-core") {
//                groupId = "de.tillhub.printengine"
//                artifactId = "core"
//                version = Configs.VERSION_CODE
//
//                from(components.getByName("release"))
//            }
//        }
//    }
// }
