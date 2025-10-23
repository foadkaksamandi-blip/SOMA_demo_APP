plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.soma.consumer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.soma.consumer"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // برای IntentIntegrator (در Activity نتیجه را می‌گیریم)
        multiDexEnabled = false
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        viewBinding = true
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/NOTICE",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE.txt"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    // ZXing برای اسکن QR با IntentIntegrator
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.2")
}
