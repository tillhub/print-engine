plugins {
    id("com.android.library")
    id("kotlin-android")
    id("maven-publish")
    id("io.gitlab.arturbosch.detekt")
}

android {
    compileSdk = ConfigData.targetSdkVersion
    defaultConfig {
        minSdk = ConfigData.minSdkVersion
        ndk {
            abiFilters.add("armeabi-v7a")
        }
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        getByName(ConfigData.BuildType.RELEASE) {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = ConfigData.JAVA_VERSION
        targetCompatibility = ConfigData.JAVA_VERSION
        isCoreLibraryDesugaringEnabled = true
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = ConfigData.JVM_TARGET
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
    namespace = "de.tillhub.printengine"
}

dependencies {
    // Core Dependencies
    implementation(libs.bundles.core)
    coreLibraryDesugaring(libs.android.desugarJdkLibs)
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // Pax Dependencies
    implementation(libs.google.zxing)

    // Sunmi Dependencies
    implementation(libs.sunmi.printer)

    // Utils
    implementation(libs.timber)
    detektPlugins(libs.detekt.formatting)

    // Unit tests
    testImplementation(libs.bundles.testing)
    testImplementation(libs.bundles.robolectric)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>(ConfigData.artifactId) {
                groupId = ConfigData.applicationId
                artifactId = ConfigData.artifactId
                version = ConfigData.versionName

                from(components.getByName("release"))
            }
        }
    }
}
