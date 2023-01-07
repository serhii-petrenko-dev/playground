plugins {
    kotlin("kapt")
    kotlin("android")
    id("com.android.application")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "io.xps.playground"
    compileSdk = 33
    buildToolsVersion = "33.0.0"

    defaultConfig {
        applicationId = "io.xps.playground"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0.0"

        vectorDrawables {
            useSupportLibrary = true
        }

        resourceConfigurations.addAll(arrayOf("en", "uk"))

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isMinifyEnabled
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.0-alpha02"
    }
}

android.sourceSets.all {
    java.srcDir("src/$name/kotlin")
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.work:work-runtime-ktx:2.7.1")
    implementation("androidx.compose.ui:ui:1.4.0-alpha03")
    implementation("androidx.appcompat:appcompat:1.7.0-alpha01")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.activity:activity-compose:1.7.0-alpha02")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.compose.material3:material3:1.1.0-alpha03")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.0-alpha03")

    implementation("com.squareup.okio:okio:3.2.0")
    implementation("com.squareup.logcat:logcat:0.1")

    implementation("io.coil-kt:coil-compose:2.1.0")
    implementation("com.google.android.material:material:1.7.0")
    implementation("com.google.accompanist:accompanist-permissions:0.24.11-rc")

    kapt("com.google.dagger:hilt-compiler:2.44")
    implementation("com.google.dagger:hilt-android:2.44")
    implementation("androidx.hilt:hilt-navigation:1.0.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    debugImplementation("androidx.compose.ui:ui-tooling:1.4.0-alpha03")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.4.0-alpha03")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.4.0-alpha03")
}