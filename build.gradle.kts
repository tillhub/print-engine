plugins {
    kotlin(Dependencies.KotlinPlugins.ANDROID)
    kotlin(Dependencies.KotlinPlugins.KAPT)
    id(Dependencies.Plugins.LIBRARY)
    id(Dependencies.Plugins.DETEKT) version Versions.Plugins.DETEKT
    id(Dependencies.Plugins.HILT)
    id(Dependencies.Plugins.PUBLISH)
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

android {
    compileSdk = ConfigData.targetSdkVersion

    defaultConfig {
        minSdk = ConfigData.minSdkVersion
        targetSdk = ConfigData.targetSdkVersion
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = ConfigData.JAVA_VERSION
        targetCompatibility = ConfigData.JAVA_VERSION
    }

    hilt {
        enableExperimentalClasspathAggregation = true
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = ConfigData.JVM_TARGET
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    // Groups
    implementDependencyGroup(Dependencies.Groups.CORE)
    // Timber
    implementation(Dependencies.Tools.TIMBER)

    // Unit tests
    implementDependencyGroup(Dependencies.Groups.TEST_LIBRARIES)
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
