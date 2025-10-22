// ===== Settings for SOMA_demo_APP =====

pluginManagement {
    repositories {
        // برای پلاگین‌های Gradle (Android, Kotlin, …)
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // اجازه ندیم هر ماژول خودش ریپوی جدا تعریف کنه
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // برای دیپندنسی‌های پروژه
        google()
        mavenCentral()
        // اگر بعداً به JitPack نیاز شد، این را فعال کن:
        // maven(url = "https://jitpack.io")
    }
}

rootProject.name = "SOMA_demo_APP"

// ماژول‌ها
include(":consumer-app", ":merchant-app")
