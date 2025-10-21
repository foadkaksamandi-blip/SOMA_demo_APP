// ===== Root Gradle File for SOMA Offline Demo =====
// این فایل فقط نسخه‌های پلاگین‌ها را تعریف می‌کند.

plugins {
    id("com.android.application") version "8.5.2" apply false
    kotlin("android") version "1.9.24" apply false
}

// هیچ repository در اینجا قرار نده
// همه در settings.gradle.kts تعریف شده‌اند.
