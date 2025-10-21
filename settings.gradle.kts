pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
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

// مرحله ۲: افزودن ماژول خریدار
include(":consumer-app")
// مرحله ۳ اضافه خواهد شد:
// include(":merchant-app")
