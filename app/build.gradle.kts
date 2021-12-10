plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "siarhei.luskanau.managed.virtual.device"
        minSdk = 24
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
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
    buildFeatures {
        viewBinding = true
    }
    testOptions {
        emulatorSnapshots {
            enableForTestFailures = false
        }
        devices {
            devices.create<com.android.build.api.dsl.ManagedVirtualDevice>("testEmulator29") {
                device = "Nexus 6"
                apiLevel = 29
                systemImageSource = "aosp"
                abi = "x86"
            }
            devices.create<com.android.build.api.dsl.ManagedVirtualDevice>("testEmulator30") {
                device = "Pixel 2"
                apiLevel = 30
                systemImageSource = "google"
                abi = "x86"
            }
        }
    }
}

dependencies {
    implementation("com.google.android.material:material:1.6.0-alpha01")
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.5")
    testImplementation(kotlin("test"))
    androidTestImplementation(kotlin("test"))
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}
