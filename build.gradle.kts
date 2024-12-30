@file:Suppress("PropertyName")

import java.io.InputStream
import java.util.Properties
import org.apache.tools.ant.taskdefs.condition.Os

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlinx.kover)
    alias(libs.plugins.detekt)
}

allprojects {
    apply(from = "$rootDir/ktlint.gradle")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jetbrains.kotlinx.kover")
}

kover {
    reports {
        verify {
            rule {
                minBound(95)
                maxBound(98)
            }
        }
    }
}

subprojects {
    dependencies {
        kover(project(path))
    }
}

val CI_GRADLE = "CI_GRADLE"

tasks.register("ciBuildAndTest") {
    group = CI_GRADLE
    doLast {
        gradlew(
            "clean",
            "ktlintCheck",
            "detekt",
            "lint",
            "assembleDebug"
        )
        gradlew("test")
        gradlew(
            "koverXmlReport",
            "koverHtmlReport",
            "koverVerify"
        )
    }
}

tasks.register("ciSdkManagerLicenses") {
    group = CI_GRADLE
    doLast {
        val sdkDirPath = getAndroidSdkPath(rootDir = rootDir)
        getSdkManagerFile(rootDir = rootDir)?.let { sdkManagerFile ->
            val yesInputStream =
                object : InputStream() {
                    private val yesString = "y\n"
                    private var counter = 0

                    override fun read(): Int = yesString[counter % 2].also { counter++ }.code
                }
            providers.exec {
                executable = sdkManagerFile.absolutePath
                args = listOf("--list", "--sdk_root=$sdkDirPath")
                println("exec: ${this.commandLine.joinToString(separator = " ")}")
            }.apply { println("ExecResult: ${this.result.get()}") }
            @Suppress("DEPRECATION")
            exec {
                executable = sdkManagerFile.absolutePath
                args = listOf("--licenses", "--sdk_root=$sdkDirPath")
                standardInput = yesInputStream
                println("exec: ${this.commandLine.joinToString(separator = " ")}")
            }.apply { println("ExecResult: $this") }
        }
    }
}

tasks.register("devAll") {
    group = CI_GRADLE
    doLast {
        gradlew(
            "clean",
            "ktlintFormat",
            "ciBuildAndTest"
        )
    }
}

tasks.register("devEmulatorAll") {
    group = CI_GRADLE
    doLast {
        gradlew("ciSdkManagerLicenses")
        gradlew("cleanManagedDevices", "--unused-only")
        (27..35).forEach { apiLevelIt ->
            val name =
                listOf(
                    "managedVirtualDevice",
                    apiLevelIt.toString()
                ).joinToString(separator = "")
            gradlew(
                "${name}DebugAndroidTest",
                "-Pandroid.testInstrumentationRunnerArguments.class=" +
                    "siarhei.luskanau.managed.virtual.device.ExampleInstrumentedTest",
                "-Pandroid.testoptions.manageddevices.emulator.gpu=swiftshader_indirect",
                "--enable-display"
            )
        }
        gradlew("cleanManagedDevices")
    }
}

fun gradlew(vararg tasks: String, addToSystemProperties: Map<String, String>? = null) {
    providers.exec {
        executable =
            File(
                project.rootDir,
                if (Os.isFamily(Os.FAMILY_WINDOWS)) "gradlew.bat" else "gradlew"
            )
                .also { it.setExecutable(true) }
                .absolutePath
        args =
            mutableListOf<String>().also { mutableArgs ->
                mutableArgs.addAll(tasks)
                addToSystemProperties?.toList()?.map { "-D${it.first}=${it.second}" }?.let {
                    mutableArgs.addAll(it)
                }
                mutableArgs.add("--stacktrace")
            }
        val sdkDirPath =
            Properties().apply {
                val propertiesFile = File(rootDir, "local.properties")
                if (propertiesFile.exists()) {
                    load(propertiesFile.inputStream())
                }
            }.getProperty("sdk.dir")
        if (sdkDirPath != null) {
            val platformToolsDir = "$sdkDirPath${java.io.File.separator}platform-tools"
            val pahtEnvironment = System.getenv("PATH").orEmpty()
            if (!pahtEnvironment.contains(platformToolsDir)) {
                environment =
                    environment.toMutableMap().apply {
                        put("PATH", "$platformToolsDir:$pahtEnvironment")
                    }
            }
        }
        if (System.getenv("JAVA_HOME") == null) {
            System.getProperty("java.home")?.let { javaHome ->
                environment =
                    environment.toMutableMap().apply {
                        put("JAVA_HOME", javaHome)
                    }
            }
        }
        if (System.getenv("ANDROID_HOME") == null) {
            environment =
                environment.toMutableMap().apply {
                    put("ANDROID_HOME", "$sdkDirPath")
                }
        }
        println("commandLine: ${this.commandLine.joinToString(separator = " ")}")
    }.apply { println("ExecResult: ${this.result.get()}") }
}

fun getAndroidSdkPath(rootDir: File): String? = Properties().apply {
    val propertiesFile = File(rootDir, "local.properties")
    if (propertiesFile.exists()) {
        load(propertiesFile.inputStream())
    }
}.getProperty("sdk.dir").let { propertiesSdkDirPath ->
    (propertiesSdkDirPath ?: System.getenv("ANDROID_HOME"))
}

fun getSdkManagerFile(rootDir: File): File? =
    getAndroidSdkPath(rootDir = rootDir)?.let { sdkDirPath ->
        println("sdkDirPath: $sdkDirPath")
        val files =
            File(sdkDirPath).walk().filter { file ->
                file.path.contains("cmdline-tools") && file.path.endsWith("sdkmanager")
            }
        files.forEach { println("walk: ${it.absolutePath}") }
        val sdkmanagerFile = files.firstOrNull()
        println("sdkmanagerFile: $sdkmanagerFile")
        sdkmanagerFile
    }
