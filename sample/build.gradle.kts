plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
}

android {
    namespace = "de.tillhub.printengine.sample"
    compileSdk = Configs.COMPILE_SDK

    defaultConfig {
        applicationId = "de.tillhub.printengine.sample"
        minSdk = Configs.MIN_SDK_STAR

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

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = Configs.JVM_TARGET
        }
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.9"
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
    implementation(libs.bundles.compose)
    implementation(libs.bundles.lifecycle)
}