plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"

}

android {
    namespace = "com.adam.pertepiece"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.adam.pertepiece"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // --- SUPABASE & NETWORK START ---

    // Le coeur de Supabase (BOM pour gérer les versions automatiquement)
    implementation(platform("io.github.jan-tennert.supabase:bom:2.6.1"))

    // Les modules dont on a besoin
    implementation("io.github.jan-tennert.supabase:postgrest-kt") // Pour la Base de Données
    implementation("io.github.jan-tennert.supabase:gotrue-kt")    // Pour l'Authentification
    implementation("io.github.jan-tennert.supabase:storage-kt")   // Pour l'Upload de photos

    // Le moteur HTTP (Ktor) nécessaire pour que Supabase fonctionne
    implementation("io.ktor:ktor-client-android:2.3.12")

    // Pour transformer tes objets Kotlin en JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // --- SUPABASE & NETWORK END ---
}