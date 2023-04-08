@file:Suppress("UnstableApiUsage")

plugins {
    kotlin("kapt")
    kotlin("android")
    kotlin("plugin.serialization") version "1.8.10"
    id("com.android.application")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("org.jlleitschuh.gradle.ktlint") version "11.3.1"
    id("io.gitlab.arturbosch.detekt") version "1.22.0"
}

android {
    namespace = "io.xps.playground"
    compileSdk = 33
    compileSdkPreview = "UpsideDownCake"
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

        resourceConfigurations.addAll(arrayOf("en", "ar"))

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isPseudoLocalesEnabled = true
        }

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
        kotlinCompilerExtensionVersion = "1.4.0"
    }
}

android.sourceSets.all {
    java.srcDir("src/$name/kotlin")
}

detekt {
    config = files("$rootDir/.detekt/config.yml")
}

ktlint {
    verbose.set(true)
    android.set(true)
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(false)
    enableExperimentalRules.set(true)
    @Suppress("DEPRECATION")
    disabledRules.set(setOf("no-wildcard-imports", "import-ordering"))
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    implementation("androidx.compose.ui:ui:1.4.0")
    implementation("androidx.appcompat:appcompat:1.7.0-alpha02")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.activity:activity-compose:1.8.0-alpha02")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.compose.material3:material3:1.1.0-beta01")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.1")
    implementation("androidx.compose.material:material-icons-extended:1.3.1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0-RC")

    implementation("com.squareup.okio:okio:3.2.0")
    implementation("com.squareup.logcat:logcat:0.1")

    implementation("io.coil-kt:coil-compose:2.2.0")
    implementation("com.google.android.material:material:1.8.0")
    implementation("com.google.accompanist:accompanist-permissions:0.24.11-rc")
    implementation("com.google.firebase:firebase-crashlytics-ktx:18.3.6")
    implementation("com.google.firebase:firebase-analytics-ktx:21.2.1")

    kapt("com.google.dagger:hilt-compiler:2.44")
    implementation("com.google.dagger:hilt-android:2.44")
    implementation("androidx.hilt:hilt-navigation:1.0.0")

    implementation("com.google.android.exoplayer:exoplayer:2.18.5")

    debugImplementation("androidx.compose.ui:ui-tooling:1.4.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.4.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.4.0")
}
