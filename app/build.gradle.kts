import com.android.build.api.dsl.ManagedVirtualDevice

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
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
    kotlinOptions {
        jvmTarget = libs.versions.build.jvmTarget.get()
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests {
            all { test: Test ->
                test.testLogging.events =
                    setOf(
                        org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
                        org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
                        org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
                    )
            }
        }
        animationsDisabled = true
        emulatorSnapshots {
            enableForTestFailures = false
        }
        (27..35).forEach { apiLevelIt ->
            val name =
                listOf(
                    "managedVirtualDevice",
                    apiLevelIt.toString()
                ).joinToString(separator = "")
            managedDevices.devices.create<ManagedVirtualDevice>(name) {
                device = "Nexus 4"
                apiLevel = apiLevelIt
                val systemImageConfig: Pair<String?, Boolean?> = when (apiLevel) {
                    33, 34 -> "aosp" to true
                    35 -> "google" to null
                    else -> null to null
                }
                systemImageConfig.first?.also { systemImageSource = it }
                systemImageConfig.second?.also { require64Bit = it }
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(kotlin("test"))
    androidTestImplementation(kotlin("test"))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
