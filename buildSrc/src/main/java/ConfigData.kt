import org.gradle.api.JavaVersion

object ConfigData {
    const val artifactId = "print-engine"
    const val applicationId = "de.tillhub.printengine"
    const val minSdkVersion = 21
    const val targetSdkVersion = 32
    const val versionCode = 15
    const val versionName = "1.4.0"

    val JAVA_VERSION = JavaVersion.VERSION_17
    val JVM_TARGET = JAVA_VERSION.toString()

    object BuildType {
        const val DEBUG = "debug"
        const val RELEASE = "release"
    }
}
