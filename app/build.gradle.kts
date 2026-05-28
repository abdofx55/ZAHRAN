plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.zahran"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.zahran"
        minSdk = 24
        targetSdk = 35
        versionCode = 4
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        // Enable K2-specific options if desired or leave to defaults
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // AndroidX Core and Lifecycles
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose (using BoM versioning)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling-preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Navigation Compose
    implementation(libs.androidx.navigation-compose)

    // Splash Screen API
    implementation(libs.androidx.core.splashscreen)

    // Dagger-Hilt DI
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    annotationProcessor(libs.hilt.compiler) // For kapt/ksp later if needed, but Hilt provides standard annotationProcessor/ksp

    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json)

    // Firebase (Firestore + FCM + Analytics) via Firebase BoM
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebase.analytics-ktx)
}
