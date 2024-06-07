plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
}

android {
    namespace = "de.tillhub.printengine.sample"
    compileSdk = 34

    defaultConfig {
        applicationId = "de.tillhub.printengine.sample"
        minSdk = 24
        targetSdk = 34

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

    implementation(libs.androidx.core)
    implementation(libs.bundles.compose)
    implementation(libs.bundles.lifecycle)
}