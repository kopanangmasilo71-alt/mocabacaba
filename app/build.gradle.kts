plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.masilo.morabarabacollection"
    compileSdk = 37

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
    defaultConfig {
        applicationId = "com.masilo.morabarabacollection"
        minSdk = 24
        targetSdk = 37
        versionCode = 20
        versionName = "20.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("nl.dionsegijn:konfetti-xml:2.0.4")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation(libs.play.services.ads)
    implementation(libs.user.messaging.platform)
    
    // Explicitly add Room and WorkManager to fix ProGuard resolution and startup crashes
    implementation("androidx.work:work-runtime:2.10.0")
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    implementation(libs.circleimageview)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}