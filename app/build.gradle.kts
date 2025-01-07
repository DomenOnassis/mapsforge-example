plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.mapsforge_application"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mapsforge_application"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //mapsforge
    implementation(libs.mapsforge.map.android)
    implementation(libs.mapsforge.map)
    implementation(libs.mapsforge.themes)
    implementation(libs.mapsforge.map.reader)
    implementation(libs.mapsforge.core)

    /* or
        implementation("org.mapsforge:mapsforge-map-android:0.16.0")
        implementation("org.mapsforge:mapsforge-map:0.16.0")
        implementation("org.mapsforge:mapsforge-themes:0.16.0")
        implementation("org.mapsforge:mapsforge-map-reader:0.16.0")
        implementation("org.mapsforge:mapsforge-core:0.16.0")
    * */
}