// Top-level build file for SOMA_demo_APP

plugins {
    id("com.android.application") version "8.7.0" apply false
    kotlin("android") version "1.9.24" apply false
}

// هیچ repositoriesاینجا نگذار—مخازن فقط در settings.gradle.kts تعریف شوند.

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
```0
