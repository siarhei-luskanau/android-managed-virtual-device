import com.android.build.api.dsl.ManagedVirtualDevice

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 33
    buildToolsVersion = "33.0.0"

    defaultConfig {
        applicationId = "siarhei.luskanau.managed.virtual.device"
        minSdk = 24
        targetSdk = 33
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
        listOf(29, 30, 31, 32, 33).forEach { apiLevelIt ->
            val name = listOf(
                "managedVirtualDevice",
                apiLevelIt.toString()
            ).joinToString(separator = "")
            managedDevices.devices.create<ManagedVirtualDevice>(name) {
                device = "Nexus 4"
                apiLevel = apiLevelIt
            }
        }
    }
}

dependencies {
    implementation("com.google.android.material:material:1.8.0-alpha01")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.1")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.1")
    testImplementation(kotlin("test"))
    androidTestImplementation(kotlin("test"))
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}
