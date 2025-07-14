plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.detekt)
    id("maven-publish")
}

android {
    namespace = Configs.APPLICATION_ID + ".sunmi"
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
                "proguard-rules.pro"
            )
            consumerProguardFiles(
                "consumer-rules.pro"
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

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = Configs.JVM_TARGET
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
    implementation(project(":print-engine"))

    // Core Dependencies
    implementation(libs.bundles.core)
    coreLibraryDesugaring(libs.android.desugarJdkLibs)

    // Sunmi Dependencies
    implementation(libs.sunmi.printer)

    // Utils
    implementation(libs.timber)

    // Unit tests
    testImplementation(libs.bundles.testing)
    testImplementation(libs.bundles.robolectric)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release-sunmi") {
                groupId = "de.tillhub.printengine"
                artifactId = "sunmi"
                version = Configs.VERSION_CODE

                from(components.getByName("release"))
            }
        }
    }
}
