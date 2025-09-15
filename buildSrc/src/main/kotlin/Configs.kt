import org.gradle.api.JavaVersion

object Configs {
    const val APPLICATION_ID = "de.tillhub.printengine"
    const val COMPILE_SDK = 36
    const val MIN_SDK = 24
    const val VERSION_CODE = "2.3.0"
    val JAVA_VERSION = JavaVersion.VERSION_17
    val JVM_TARGET = JAVA_VERSION.toString()
}
