// SOMA_demo_APP - Root Gradle
val kotlinVersion = providers.gradleProperty("kotlin.version").get()
val agpVersion = providers.gradleProperty("agp.version").get()

plugins {
    id("com.android.application") version agpVersion apply false
    kotlin("android") version kotlinVersion apply false
}

// تسک تمیزکننده ریشه
tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
