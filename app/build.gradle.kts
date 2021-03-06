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
        listOf(27, 28, 29, 30, 31, 32, 33).forEach { apiLevelIt ->
            listOf("google", "google-atd", "aosp", "aosp-atd").forEach { systemImageSourceIt ->
                listOf("", "64Bit").forEach { require64BitVariant ->
                    val name = listOf(
                        "managedVirtualDevice",
                        apiLevelIt.toString(),
                        systemImageSourceIt.capitalize(),
                        require64BitVariant.capitalize()
                    ).joinToString(separator = "")
                    managedDevices.devices.create<ManagedVirtualDevice>(name) {
                        device = "Nexus 4"
                        apiLevel = apiLevelIt
                        systemImageSource = systemImageSourceIt
                        require64Bit = require64BitVariant.isNotBlank()
                    }
                }
            }
        }
    }
}

dependencies {
    implementation("com.google.android.material:material:1.7.0-alpha02")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.0")
    testImplementation(kotlin("test"))
    androidTestImplementation(kotlin("test"))
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}
