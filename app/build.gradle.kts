@file:Suppress("DEPRECATION")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.devtools.ksp)
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.todoapp"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.todoapp"
        minSdk = 24
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
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
//    kotlin {
//        jvmToolchain(11)
//    }
    buildFeatures {
        viewBinding = true
    }
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental", "true")
        arg("room.expandProjection", "true")
    }
}

dependencies {
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.work.runtime)
    implementation(libs.squareup.retrofit2)
    implementation(libs.converter.gson)
    implementation(libs.squareup.okhttp3.okhttp)
    implementation(libs.squareup.okhttp3.logging.interceptor)
    implementation(libs.androidx.splashscreen)
    implementation(libs.mpandroidChart)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}