import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) // یا همین مقدار را نگه‌دار
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SOMA_demo_APP"
include(":consumer-app", ":merchant-app")
