import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.maven.publish)
}

object EpsonSdk {
    const val VERSION = "2.23.1"
    const val REPO = "https://github.com/popina/EpsonSDK.git"
    const val FRAMEWORK_NAME = "print-engine-epson"

    // DEVICE_SLICE  → all physical iPhones/iPads (arm64 + legacy armv7)
    // SIMULATOR_SLICE → Xcode Simulator on Intel & Apple Silicon Macs
    const val DEVICE_SLICE = "ios-arm64_armv7"
    const val SIMULATOR_SLICE = "ios-arm64_i386_x86_64-simulator"

    fun sliceFor(target: KonanTarget): String =
        when (target) {
            KonanTarget.IOS_ARM64 -> DEVICE_SLICE
            else -> SIMULATOR_SLICE
        }
}

val epsonXcframeworkDir = "${project.projectDir}/nativeInterop/libs/ios/libepos2.xcframework"

val downloadEpsonSdk by tasks.registering {
    group = "setup"
    description = "Downloads Epson ePOS SDK xcframework if not already present"
    outputs.dir(epsonXcframeworkDir)
    onlyIf { !file(epsonXcframeworkDir).exists() }

    doLast {
        val tmpDir =
            layout.buildDirectory
                .dir("tmp/epson-sdk")
                .get()
                .asFile
        tmpDir.deleteRecursively()
        tmpDir.mkdirs()

        logger.lifecycle("Downloading Epson ePOS SDK v${EpsonSdk.VERSION} ...")
        providers
            .exec {
                commandLine(
                    "git",
                    "clone",
                    "--depth",
                    "1",
                    "--branch",
                    EpsonSdk.VERSION,
                    EpsonSdk.REPO,
                    tmpDir.absolutePath,
                )
            }.result
            .get()

        file(epsonXcframeworkDir).parentFile.mkdirs()
        copy {
            from("${tmpDir.absolutePath}/libepos2.xcframework")
            into(epsonXcframeworkDir)
        }

        tmpDir.deleteRecursively()
        logger.lifecycle("Epson ePOS SDK installed at $epsonXcframeworkDir")
    }
}

tasks.configureEach {
    if (name.startsWith("cinteropEpos2")) dependsOn(downloadEpsonSdk)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }

        publishLibraryVariants("release")

        dependencies {
            implementation(libs.androidx.core)
            coreLibraryDesugaring(libs.android.desugarJdkLibs)
            implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

            testImplementation(libs.bundles.testing)
            testImplementation(libs.bundles.robolectric)

            androidTestImplementation(libs.bundles.testing)
            androidTestImplementation(libs.bundles.testing.android)
        }
    }

    fun org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget.configureEpsonInterop() {
        val slice = EpsonSdk.sliceFor(konanTarget)
        binaries.framework { baseName = EpsonSdk.FRAMEWORK_NAME }
        compilations["main"].cinterops {
            create("epos2") {
                definitionFile = file("nativeInterop/cinterop/epos2.def")
                includeDirs("$epsonXcframeworkDir/$slice/Headers")
            }
        }
        binaries.all {
            linkerOpts(
                "-L$epsonXcframeworkDir/$slice",
                "-lepos2",
                "-framework",
                "CoreBluetooth",
                "-framework",
                "ExternalAccessory",
            )
        }
    }

    iosArm64 { configureEpsonInterop() }
    iosX64 { configureEpsonInterop() }
    iosSimulatorArm64 { configureEpsonInterop() }

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
        val androidInstrumentedTest by getting {
            kotlin.srcDirs("src/androidInstrumentedTest/kotlin")
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

android {
    namespace = Configs.APPLICATION_ID + ".epson"
    compileSdk = Configs.COMPILE_SDK

    defaultConfig {
        minSdk = Configs.MIN_SDK
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    packaging {
        resources {
            excludes.add("META-INF/*")
            excludes.add("MANIFEST.MF")
        }
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
        artifactId = "print-engine-epson",
        version =
            libs.versions.print.engine
                .get(),
    )

    // Configure POM metadata for the published artifact
    pom {
        name.set("Epson Print Engine plugin")
        description.set("Kotlin MultiPlatform Library printer implementation for Epson devices")
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
