plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "app.vidgen"
    compileSdk = 34

    defaultConfig {
        applicationId = "app.vidgen"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    flavorDimensions += "tier"
    productFlavors {
        // Option 1: tiny, fully offline procedural generator, zero dependencies
        create("lite") { dimension = "tier" }
        // Options 2/3: same app + optional on-device AI model (ONNX Runtime)
        create("ai") {
            dimension = "tier"
            applicationIdSuffix = ".ai"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            // Signed with the debug key so the APK installs directly.
            // Use your own keystore if you publish the app.
            signingConfig = signingConfigs.getByName("debug")
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
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    "aiImplementation"("com.microsoft.onnxruntime:onnxruntime-android:1.19.2")
}
