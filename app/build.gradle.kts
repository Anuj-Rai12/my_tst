plugins {
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.pos10"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pos10"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "ENABLE_LOGS", "true")
            buildConfigField("String", "BASE_URL", "\"https://fmsapi.sea-pos.com/\"")
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("boolean", "ENABLE_LOGS", "false")
            buildConfigField("String", "BASE_URL", "\"https://fosapi.pay10.asia/\"")

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
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
        compose = true
    }

    kapt {
        correctErrorTypes = true
    }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }

}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.appcompat)
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.transportation.consumer)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.androidx.localbroadcastmanager)
    implementation(libs.google.maps.compose)
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions) // optional
    implementation(libs.mlkit.barcode)
    implementation(libs.zxing.core)


    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.converter.moshi)
    implementation(libs.gson)

    implementation("androidx.work:work-runtime-ktx:2.10.3")
    implementation("androidx.hilt:hilt-work:1.0.0")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Networking
    implementation(libs.logging.interceptor)

    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Room
    implementation("androidx.room:room-runtime:2.7.0-alpha07")
    kapt("androidx.room:room-compiler:2.7.0-alpha07")
    implementation("androidx.room:room-ktx:2.7.0-alpha07")

    implementation("com.google.accompanist:accompanist-swiperefresh:0.36.0")

    //snackbar
    implementation("com.google.android.material:material:1.12.0")
    // Other libs...

    // Fingerprint / Biometric + Secure PIN
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

}
