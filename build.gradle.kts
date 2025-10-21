// --- read versions from gradle.properties (with safe fallback) ---
val kotlinVersion: String = providers.gradleProperty("kotlin.version").orNull ?: "1.9.24"
val agpVersion: String = providers.gradleProperty("agp.version").orNull ?: "8.5.2"

plugins {
    id("com.android.application") version agpVersion apply false
    id("com.android.library") version agpVersion apply false
    kotlin("android") version kotlinVersion apply false
    // در صورت نیاز به Serialization/Parcelize/… همینجا اضافه کنید:
    // id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
