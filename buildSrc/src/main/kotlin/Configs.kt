import org.gradle.api.JavaVersion

object Configs {
    const val APPLICATION_ID = "de.tillhub.printengine"
    const val COMPILE_SDK = 34
    const val MIN_SDK = 23
    const val VERSION_CODE = "2.3.2"
    val JAVA_VERSION = JavaVersion.VERSION_17
    val JVM_TARGET = JAVA_VERSION.toString()
}
