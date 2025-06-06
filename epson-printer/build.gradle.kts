plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.detekt)
    id("maven-publish")
}

android {
    namespace = Configs.APPLICATION_ID + ".epson"
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

    // Android tests
    androidTestImplementation(libs.bundles.testing.android)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release-epson") {
                groupId = "de.tillhub.printengine"
                artifactId = "epson"
                version = Configs.VERSION_CODE

                from(components.getByName("release"))
            }
        }
        repositories {
            maven {
                url = uri("https://nexus.infra.unzer.io/repository/tillhub-print-engine-epson-hosted/")
                credentials {
                    username = System.getenv("NEXUS_USERNAME")
                    password = System.getenv("NEXUS_PASSWORD")
                }
            }
        }
    }
}
