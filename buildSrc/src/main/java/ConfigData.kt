import org.gradle.api.JavaVersion

object ConfigData {
    const val artifactId = "print-engine"
    const val applicationId = "de.tillhub.printengine"
    const val minSdkVersion = 21
    const val targetSdkVersion = 32
    const val versionCode = 8
    const val versionName = "1.0.8"

    val JAVA_VERSION = JavaVersion.VERSION_11
    val JVM_TARGET = JavaVersion.VERSION_11.toString()

    object BuildType {
        const val DEBUG = "debug"
        const val RELEASE = "release"
    }
}
