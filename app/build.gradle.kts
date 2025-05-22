plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt") // Keep kapt plugin
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.taller3"
    compileSdk = 35

    defaultConfig {
        applicationId ="com.example.taller3"
        minSdk =24
        targetSdk =35
        versionCode =1
        versionName ="1.0"

        testInstrumentationRunner= "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility =JavaVersion.VERSION_11
                targetCompatibility =JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose= true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation (platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Firebase (Ensure versions are compatible with BOM)
    implementation (platform("com.google.firebase:firebase-bom:32.7.0"))  // Use the BOM
    implementation ("com.google.firebase:firebase-auth-ktx")
    implementation ("com.google.firebase:firebase-firestore-ktx")
    implementation ("com.google.firebase:firebase-storage-ktx")
    implementation ("androidx.compose.material:material-icons-extended:1.6.5")

    // Jetpack Compose
    implementation ("androidx.activity:activity-compose:1.8.2")
    implementation ("androidx.compose.ui:ui:1.6.5")
    implementation ("androidx.compose.material3:material3:1.1.2")
    implementation ("androidx.navigation:navigation-compose:2.7.7")

    // Maps and Location
    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.google.maps.android:maps-compose:2.11.4")


    // Hilt for DI
    implementation ("com.google.dagger:hilt-android:2.49")  // Corrected: Not a platform dependency
    kapt ("com.google.dagger:hilt-android-compiler:2.49")  // Keep kapt for the compiler
    implementation ("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Kotlin Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation("io.coil-kt:coil-compose:2.4.0")


}
