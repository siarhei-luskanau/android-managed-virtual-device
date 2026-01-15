plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "siarhei.luskanau.managed.virtual.device"
    compileSdk = libs.versions.build.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "siarhei.luskanau.managed.virtual.device"
        minSdk = libs.versions.build.android.minSdk.get().toInt()
        targetSdk = libs.versions.build.android.targetSdk.get().toInt()
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
        sourceCompatibility = JavaVersion.valueOf(libs.versions.build.javaVersion.get())
        targetCompatibility = JavaVersion.valueOf(libs.versions.build.javaVersion.get())
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests {
            all { test: Test ->
                test.testLogging.events =
                    org.gradle.api.tasks.testing.logging.TestLogEvent.values().toSet()
                test.testLogging.exceptionFormat =
                    org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            }
        }
        animationsDisabled = true
        (27..36).forEach { apiLevelIt ->
            val name =
                listOf(
                    "managedVirtualDevice",
                    apiLevelIt.toString()
                ).joinToString(separator = "")
            managedDevices.localDevices.create(name) {
                device = "Nexus 4"
                apiLevel = apiLevelIt
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(platform(libs.androidx.compose.bom))
    testImplementation(kotlin("test-junit"))
    androidTestImplementation(kotlin("test-junit"))
    androidTestImplementation(libs.androidx.espresso.core)
}
