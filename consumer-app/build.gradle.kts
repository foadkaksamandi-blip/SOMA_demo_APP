plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
}

android {
    namespace "com.soma.consumer"
    compileSdk 33

    defaultConfig {
        applicationId "com.soma.consumer"
        minSdk 24
        targetSdk 33
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
        debug {
            minifyEnabled false
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = '17'
    }

    buildFeatures {
        viewBinding true
        dataBinding true
    }

    packagingOptions {
        resources.excludes += [
            'META-INF/DEPENDENCIES',
            'META-INF/LICENSE',
            'META-INF/LICENSE.txt',
            'META-INF/license.txt',
            'META-INF/NOTICE',
            'META-INF/NOTICE.txt',
            'META-INF/notice.txt',
            'META-INF/ASL2.0'
        ]
    }
}

dependencies {

    // AndroidX Base
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.7.0'
    implementation 'com.google.android.material:material:1.12.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'

    // Lifecycle & ViewModel
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'

    // ZXing QR Scanner
    implementation 'com.journeyapps:zxing-android-embedded:4.3.0'
    implementation 'com.google.zxing:core:3.5.1'

    // Bluetooth Low Energy helpers (اختیاری برای BLEClient)
    implementation 'no.nordicsemi.android:ble:2.6.1'

    // Logging & Kotlin utils
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1'
    implementation 'com.jakewharton.timber:timber:5.0.1'

    // Testing
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.2.1'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.6.1'
}
