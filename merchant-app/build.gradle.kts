plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.soma.merchant"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.soma.merchant"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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

    // ما از XML لایه‌ها استفاده می‌کنیم
    buildFeatures {
        viewBinding = true
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
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Material 3 (برای استایل‌های Filled/Outlined و اتربیوت‌های materialButton*)
    implementation("com.google.android.material:material:1.13.0-alpha05")

    // (اختیاری) لاگ و کمک‌ها
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.5") // فقط برای Preview/Tooling بی‌اثر روی XML
}
