plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.soma.consumer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.soma.consumer"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
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

    // برای مشکلات viewBinding / dataBinding در MainActivity
    buildFeatures {
        viewBinding = true
        // اگر واقعاً DataBinding استفاده نمی‌کنی، همین کافی است.
        // dataBinding = true  // فقط در صورت نیاز
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources.excludes += setOf("META-INF/*")
    }
}

dependencies {
    // اندروید پایه
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-ktx:1.9.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // اسکن QR با ZXing (Core)
    implementation("com.google.zxing:core:3.5.3")

    // اگر از CameraX برای QR استفاده می‌کنی (اختیاری):
    // implementation("androidx.camera:camera-core:1.3.4")
    // implementation("androidx.camera:camera-camera2:1.3.4")
    // implementation("androidx.camera:camera-lifecycle:1.3.4")
}
