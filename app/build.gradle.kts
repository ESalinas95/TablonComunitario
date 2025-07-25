plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.tabloncomunitario"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.tabloncomunitario"
        minSdk = 24
        targetSdk = 35
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
        viewBinding = true
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
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.media3.common.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.glide)
    annotationProcessor(libs.compiler)
    implementation(libs.circleimageview)

    // Room components
    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)
    // To use Kotlin annotation processing tool (kapt)
    //noinspection KaptUsageInsteadOfKsp
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Dependencias de Jetpack Compose
    implementation(platform(libs.androidx.compose.bom.v20240400)) // <--- VERSIÓN MÁS RECIENTE
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3) // O material:material si no usas Material 3

    // Dependencias de Jetpack Compose para Activities
    implementation(libs.androidx.activity.compose.v190) // <--- VERSIÓN MÁS RECIENTE

    // Dependencias de Tooling (para previsualizar Composables en Android Studio)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

    // Dependencias de Test para Compose
    androidTestImplementation(libs.ui.test.junit4)
    implementation(libs.androidx.compose.material3.material3) // <--- Asegúrate de que esta dependencia esté

    implementation(libs.androidx.navigation.compose) // <--- VERSIÓN MÁS RECIENTE

    implementation(libs.coil.compose)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.foundation)


    implementation(libs.androidx.lifecycle.viewmodel.ktx) // O la versión más reciente
    // ViewModel utilities for Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose) // O la versión más reciente
    implementation(libs.kotlinx.coroutines.play.services) // ¡Verifica la versión más reciente si es posible!

    // LiveData (si decides usar LiveData en ViewModels)
    implementation(libs.androidx.lifecycle.livedata.ktx) // O la versión más reciente

}