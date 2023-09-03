import com.android.build.api.dsl.ManagedVirtualDevice

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "siarhei.luskanau.managed.virtual.device"
    compileSdk = 34

    defaultConfig {
        applicationId = "siarhei.luskanau.managed.virtual.device"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.valueOf(libs.versions.build.javaVersion.get())
        targetCompatibility = JavaVersion.valueOf(libs.versions.build.javaVersion.get())
    }
    kotlinOptions {
        jvmTarget = libs.versions.build.jvmTarget.get()
    }
    buildFeatures {
        viewBinding = true
    }
    testOptions {
        unitTests {
            all { test: Test ->
                test.testLogging.events = setOf(
                    org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
                    org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
                    org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
                )
            }
        }
        animationsDisabled = true
        emulatorSnapshots {
            enableForTestFailures = false
        }
        (27..34).forEach { apiLevelIt ->
            val name = listOf(
                "managedVirtualDevice",
                apiLevelIt.toString(),
            ).joinToString(separator = "")
            managedDevices.devices.create<ManagedVirtualDevice>(name) {
                device = "Nexus 4"
                apiLevel = apiLevelIt
            }
        }
    }
}

dependencies {
    implementation(libs.android.material)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(kotlin("test"))
    androidTestImplementation(kotlin("test"))
    androidTestImplementation(libs.espresso.core)
}
