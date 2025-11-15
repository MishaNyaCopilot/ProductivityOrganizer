// Корневой build.gradle.kts
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.hiltAndroid) apply false // Используем alias вместо id
    alias(libs.plugins.ksp) apply false // Добавляем KSP сюда
}
true // Needed to make the script valid