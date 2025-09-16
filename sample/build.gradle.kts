import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
}

kotlin {
    compilerOptions {
        // removes warnings for expect/actual classes
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }

        dependencies {
            // Android desugaring must stay on the Android configuration level
            coreLibraryDesugaring(libs.android.desugarJdkLibs)
        }
    }

    val xcfName = "ComposeApp"
    iosX64 {
        binaries.framework {
            baseName = xcfName
            isStatic = true
        }
    }
    iosArm64 {
        binaries.framework {
            baseName = xcfName
            isStatic = true
        }
    }
    iosSimulatorArm64 {
        binaries.framework {
            baseName = xcfName
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":print-engine"))
                implementation(project(":print-plugins:star"))
                implementation(project(":print-plugins:epson"))

                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(compose.material3)
                implementation(compose.components.uiToolingPreview)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(project(":print-plugins:pax"))
                implementation(project(":print-plugins:sunmi"))
                implementation(project(":print-plugins:verifone"))

                implementation(libs.androidx.core)
                implementation(libs.kotlin.coroutines)
                implementation(libs.bundles.lifecycle)

                implementation(libs.activity.compose)
            }
        }
    }
}

android {
    namespace = "de.tillhub.printengine.sample"
    compileSdk = Configs.COMPILE_SDK

    defaultConfig {
        applicationId = "de.tillhub.printengine.sample"
        minSdk = Configs.MIN_SDK

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
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
}

dependencies {
    debugImplementation(compose.uiTooling)
}
