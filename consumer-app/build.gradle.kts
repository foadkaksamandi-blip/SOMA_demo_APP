android {
    namespace = "com.soma.consumer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.soma.consumer"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}
