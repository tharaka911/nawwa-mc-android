plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "lk.macna.nawwa_mc"
    compileSdk = 36

    defaultConfig {
        applicationId = "lk.macna.nawwa_mc"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Set BASE_URL from gradle.properties (Default if not found)
        val baseUrl = project.findProperty("BASE_URL") ?: "https://ecom-api.macna.app"
        buildConfigField("String", "BASE_URL", baseUrl.toString())
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.browser)
    
    implementation(libs.okhttp)
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)
    
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}