// پروژه‌-سطح (Root) - فقط تعریف نسخهٔ پلاگین‌ها
plugins {
    id("com.android.application") version "8.5.2" apply false
    kotlin("android") version "1.9.24" apply false
}

// هیچ repositories اینجا نگذار؛ طبق Settings مدیریت می‌شود.
