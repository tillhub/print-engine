plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.detekt)
    id("maven-publish")
}

android {
    namespace = "de.tillhub.printengine.epson"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
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


detekt {
    buildUponDefaultConfig = true // preconfigure defaults
    allRules = false // activate all available (even unstable) rules.
    config.setFrom("$rootDir/print-engine/config/detekt.yml")
}

dependencies {
    implementation(project(":print-engine"))

    // Core Dependencies
    implementation(libs.bundles.core)
    coreLibraryDesugaring(libs.android.desugarJdkLibs)
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // Utils
    implementation(libs.timber)
    detektPlugins(libs.detekt.formatting)
    detektPlugins(libs.detekt.libraries)

    // Unit tests
    testImplementation(libs.bundles.testing)
    testImplementation(libs.bundles.robolectric)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("print-engine:epson") {
                groupId = "de.tillhub.printengine.epson"
                artifactId = "print-engine:epson"
                version = "1.8.0"

                from(components.getByName("release"))
            }
        }
    }
}
