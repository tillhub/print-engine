import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.maven.publish)
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

            // Printer Dependencies
            implementation(libs.star.printer)

            // Unit tests
            testImplementation(libs.bundles.testing)
            testImplementation(libs.bundles.robolectric)
        }
    }

    val xcfName = "print-engine-star"
    iosX64 { binaries.framework { baseName = xcfName } }
    iosArm64 { binaries.framework { baseName = xcfName } }
    iosSimulatorArm64 { binaries.framework { baseName = xcfName } }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":print-engine"))

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
            kotlin.srcDirs("src/androidMain/kotlin")
            dependencies {
                implementation(libs.kotlin.coroutines.android)
            }
        }
        val androidUnitTest by getting {
            kotlin.srcDirs("src/androidUnitTest/kotlin")
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

android {
    namespace = Configs.APPLICATION_ID + ".star"
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

mavenPublishing {
    // Define coordinates for the published artifact
    coordinates(
        groupId = "io.github.tillhub",
        artifactId = "print-engine-star",
        version =
            libs.versions.print.engine
                .get(),
    )

    // Configure POM metadata for the published artifact
    pom {
        name.set("Start Print Engine plugin")
        description.set("Kotlin MultiPlatform Library printer implementation for Star printers")
        inceptionYear.set("2025")
        url.set("https://github.com/tillhub/print-engine")

        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        // Specify developers information
        developers {
            developer {
                id.set("djordjeh")
                name.set("Đorđe Hrnjez")
                email.set("dorde.hrnjez@unzer.com")
            }
            developer {
                id.set("SloInfinity")
                name.set("Martin Sirok")
                email.set("m.sirok.ext@unzer.com")
            }
            developer {
                id.set("shekar-allam")
                name.set("Chandrashekar Allam")
                email.set("chandrashekar.allam@unzer.com")
            }
        }

        // Specify SCM information
        scm {
            url.set("https://github.com/tillhub/print-engine")
        }
    }

    // Configure publishing to Maven Central
    publishToMavenCentral()

    // Enable GPG signing for all publications
    signAllPublications()
}
