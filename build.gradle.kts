plugins {
    id("com.android.application") version "8.7.0" apply false
    kotlin("android") version "1.9.24" apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
