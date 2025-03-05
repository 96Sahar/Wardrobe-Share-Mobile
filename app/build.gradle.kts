import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.androidx.navigation.safeargs)
    id("kotlin-kapt")
    alias(libs.plugins.google.services)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

// Retrieve the Cloudinary properties
val cloudinaryCloudName: String = localProperties.getProperty("cloud_name") ?: ""
val cloudinaryApiKey: String = localProperties.getProperty("cloudinary_api_key") ?: ""
val cloudinaryApiSecret: String = localProperties.getProperty("cloudinary_api_secret") ?: ""
val apiSecretGemini: String = localProperties.getProperty("api_secret_gemini") ?: ""

android {
    buildFeatures {
        buildConfig = true
        dataBinding = true
    }
    namespace = "com.example.wardrobe_share"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.wardrobe_share"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"$cloudinaryCloudName\"")
        buildConfigField("String", "CLOUDINARY_API_KEY", "\"$cloudinaryApiKey\"")
        buildConfigField("String", "CLOUDINARY_API_SECRET", "\"$cloudinaryApiSecret\"")
        buildConfigField("String", "GEMINI_API_KEY", "\"$apiSecretGemini\"")
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
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.circleimageview)
    implementation(libs.glide)
    annotationProcessor(libs.compiler)

    implementation(libs.picasso)
    implementation(libs.cloudinary.android)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    implementation(libs.ktor.serialization.gson)

    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Retrofit and Gson converter
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // OkHttp (and optional logging interceptor)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Kotlin Coroutines (if you are using suspend functions)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // RecyclerView (for the chat messages list)
    implementation("androidx.recyclerview:recyclerview:1.2.1")

// ConstraintLayout (already included via `libs.androidx.constraintlayout`, so no need to add it again)

// ViewModel & LiveData (for managing chat state, already included if `androidx.lifecycle` is present)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

// Retrofit and Gson (you already have these)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// OkHttp (for network requests, already included)
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

// Kotlin Coroutines (for async API calls, you already have them)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")

}