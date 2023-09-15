plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    /* Depend on the kotlin plugin, since we want to access it in our plugin */
    implementation(libs.gradlePlugin.kotlin)

    /* Depend on the android gradle plugin, since we want to access it in our plugin */
    implementation(libs.gradlePlugin.android)
}
