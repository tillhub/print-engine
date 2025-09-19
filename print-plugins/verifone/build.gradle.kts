import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.maven.publish)
}

android {
    namespace = Configs.APPLICATION_ID + ".verifone"
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

    kotlin {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(project(":print-engine"))
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // Core Dependencies
    implementation(libs.androidx.core)
    implementation(libs.kotlin.coroutines)
    coreLibraryDesugaring(libs.android.desugarJdkLibs)

    // Utils
    implementation(libs.kermit)

    // Unit tests
    testImplementation(libs.bundles.testing)
    testImplementation(libs.bundles.robolectric)
}

mavenPublishing {
    // Define coordinates for the published artifact
    coordinates(
        groupId = "io.github.tillhub",
        artifactId = "print-engine-verifone",
        version =
            libs.versions.print.engine
                .get(),
    )

    // Configure POM metadata for the published artifact
    pom {
        name.set("Verifone Print Engine plugin")
        description.set("Kotlin MultiPlatform Library printer implementation for Pax devices")
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
