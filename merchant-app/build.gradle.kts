android {
    namespace = "com.soma.merchant"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.soma.merchant"
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
