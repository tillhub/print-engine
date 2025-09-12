import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
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

    kotlin {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    coreLibraryDesugaring(libs.android.desugarJdkLibs)

    implementation(project(":print-engine"))
    implementation(project(":print-plugins:star"))
    implementation(project(":print-plugins:epson"))
    implementation(project(":print-plugins:pax"))
    implementation(project(":print-plugins:sunmi"))
    implementation(project(":print-plugins:verifone"))

    implementation(libs.androidx.core)
    implementation(libs.kotlin.coroutines)
    implementation(libs.bundles.lifecycle)

    implementation(compose.runtime)
    implementation(compose.ui)
    implementation(compose.material3)
    implementation(compose.components.uiToolingPreview)
    implementation(libs.activity.compose)
}