
object Dependencies {

    object Plugins {
        const val LIBRARY = "com.android.library"
        const val DETEKT = "io.gitlab.arturbosch.detekt"
        const val PUBLISH = "maven-publish"
    }

    object KotlinPlugins {
        const val ANDROID = "android"
        const val KAPT = "kapt"
    }

    object Tools {
        const val TIMBER = "com.jakewharton.timber:timber:${Versions.Tools.TIMBER}"
    }

    object Kotlin {
        const val COROUTINES = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.Kotlin.COROUTINES}"
        const val COROUTINES_TEST = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.Kotlin.COROUTINES}"
    }

    object AndroidX {
        const val CORE_KTX = "androidx.core:core-ktx:${Versions.AndroidX.CORE_KTX}"
    }

    object Google {
        const val ZXING = "com.google.zxing:core:${Versions.Google.ZXING}"
    }

    object Sunmi {
        const val PRINTER = "com.sunmi:printerlibrary:${Versions.Sunmi.PRINTER}"
    }

    object Testing {
        const val JUNIT = "junit:junit:${Versions.Testing.JUNIT}"
        const val MOCKK = "io.mockk:mockk:${Versions.Testing.MOCKK}"
        const val MOCKK_AGENT_JVM = "io.mockk:mockk-agent-jvm:${Versions.Testing.MOCKK}"
        const val ROBOLECTRIC = "org.robolectric:robolectric:${Versions.Testing.ROBOLECTRIC}"
        const val KOTEST_RUNNER = "io.kotest:kotest-runner-junit5:${Versions.Testing.KOTEST}"
        const val KOTEST_ASSERTIONS = "io.kotest:kotest-assertions-core:${Versions.Testing.KOTEST}"
        const val KOTEST_PROPERTY = "io.kotest:kotest-property:${Versions.Testing.KOTEST}"
        const val KOTEST_ROBOLECTRIC = "io.kotest.extensions:kotest-extensions-robolectric:${Versions.Testing.KOTEST_ROBOLECTRIC}"
    }

    object AndroidTesting {
        // Core library
        const val CORE = "androidx.test:core-ktx:${Versions.AndroidTest.CORE}"
    }

    object Modules {
        const val PAX = ":pax"
    }

    object Groups {
        val CORE = arrayListOf<Dependency>().apply {
            add(Dependency.Implementation(AndroidX.CORE_KTX))
            add(Dependency.Implementation(Kotlin.COROUTINES))
        }

        val TEST_LIBRARIES = arrayListOf<Dependency>().apply {
            add(Dependency.TestImplementation(Testing.JUNIT))
            add(Dependency.TestImplementation(Testing.MOCKK))
            add(Dependency.TestImplementation(Testing.MOCKK_AGENT_JVM))
            add(Dependency.TestImplementation(Testing.KOTEST_RUNNER))
            add(Dependency.TestImplementation(Testing.KOTEST_ASSERTIONS))
            add(Dependency.TestImplementation(Testing.KOTEST_PROPERTY))
            add(Dependency.TestImplementation(Kotlin.COROUTINES_TEST))
        }

        val TEST_ROBOLECTRIC = arrayListOf<Dependency>().apply {
            add(Dependency.TestImplementation(AndroidTesting.CORE))
            add(Dependency.TestImplementation(Testing.ROBOLECTRIC))
            add(Dependency.TestImplementation(Testing.KOTEST_ROBOLECTRIC))
        }
    }
}
