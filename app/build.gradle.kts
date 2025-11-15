// app/build.gradle.kts
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
    alias(libs.plugins.hiltAndroid)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.productivityorganizer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.productivityorganizer"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/io.netty.versions.properties"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material.icons.extended)

    // Если эти библиотеки также в BOM, то им не нужен version.ref
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.ycharts)

    // Supabase
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.client)
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.compose.auth)

    // Ktor
    implementation(libs.ktor.client.android)

    // Kotlin Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Compose Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime.android)
    implementation(libs.firebase.crashlytics.buildtools)

    // Test dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Credential Manager API
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json)

    // Notifs
    implementation(libs.androidx.work.runtime.ktx)

    // Hilt какой то
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime.android)
    // Для Room KSP:
    ksp(libs.androidx.room.compiler)

    implementation("com.google.code.gson:gson:2.10.1")

    implementation(libs.coil.compose)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
