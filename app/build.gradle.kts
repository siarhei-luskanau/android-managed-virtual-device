import com.android.build.api.dsl.ManagedVirtualDevice

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
        listOf(26, 27, 28, 29, 30, 31, 32).forEach { apiLevelIt ->
            listOf("google", "google-atd", "aosp", "aosp-atd").forEach { systemImageSourceIt ->
                listOf("x86_64", "x86").forEach { abiIt ->
                    val name = listOf(
                        "managedVirtualDevice",
                        apiLevelIt.toString(),
                        systemImageSourceIt.capitalize(),
                        abiIt.capitalize()
                    ).joinToString(separator = "")
                    devices.create<ManagedVirtualDevice>(name) {
                        device = "Nexus 4"
                        apiLevel = apiLevelIt
                        systemImageSource = systemImageSourceIt
                        abi = abiIt
                    }
                }
            }
        }
    }
}

dependencies {
    implementation("com.google.android.material:material:1.6.0-alpha02")
    implementation("androidx.navigation:navigation-fragment-ktx:2.4.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.4.0")
    testImplementation(kotlin("test"))
    androidTestImplementation(kotlin("test"))
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}
