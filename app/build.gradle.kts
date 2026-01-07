plugins {
	//id("com.example.emailapp")
    alias(libs.plugins.android.application)
	// Add the Google services Gradle plugin (должен быть после android.application)
	id("com.google.gms.google-services")
}

android {
    namespace = "com.example.emailapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.emailapp"
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
	
	// Import the Firebase BoM
  implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
  implementation("com.google.firebase:firebase-messaging")
  
    // TODO: Add the dependencies for Firebase products you want to use
  // When using the BoM, don't specify versions in Firebase dependencies
  implementation("com.google.firebase:firebase-analytics")
  
  // OkHttp для HTTP запросов
  implementation("com.squareup.okhttp3:okhttp:4.12.0")
  
}