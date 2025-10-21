pluginManagement {
    repositories {
        // برای resolve کردن پلاگین‌های اندروید از Google
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        // نسخه‌ها مطابق پروژه
        id("com.android.application") version "8.5.2" apply false
        id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SOMA_demo_APP"

// حتما اسم دایرکتوری‌ها همین است:
include(":consumer-app", ":merchant-app")
