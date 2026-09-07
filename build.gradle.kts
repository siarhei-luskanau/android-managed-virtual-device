@file:Suppress("PropertyName")

import convention.android.emulator.AndroidSdkHelper
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Properties
import org.apache.tools.ant.taskdefs.condition.Os

plugins {
    id("androidEmulatorConvention")
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.detekt)
}

allprojects {
    apply(from = "$rootDir/ktlint.gradle")
    apply(plugin = "io.gitlab.arturbosch.detekt")
}

val CI_GRADLE = "CI_GRADLE"

tasks.register("ciBuildAndTest") {
    group = CI_GRADLE
    val injected = project.objects.newInstance<Injected>()
    doLast {
        injected.gradlew(
            "clean",
            "ktlintCheck",
            "detekt",
            "lint",
            "assembleDebug"
        )
    }
}

tasks.register("setupAndroidCmdlineTools") {
    group = CI_GRADLE
    val injected = project.objects.newInstance<Injected>()
    doLast {
        AndroidSdkHelper(
            projectLayout = injected.projectLayout,
            execWrapper = object : convention.android.emulator.ExecWrapper {
                override fun exec(
                    commandLine: List<String>,
                    inputStream: InputStream?,
                    addToSystemEnvironment: Map<String, String>?
                ): String = injected.runExec(
                    commands = commandLine,
                    inputStream = inputStream,
                    addToSystemEnvironment = addToSystemEnvironment
                )
            }
        ).also {
            it.setupAndroidCmdlineTools()
        }
    }
}

tasks.register("ciSdkManagerLicenses") {
    group = CI_GRADLE
    val injected = project.objects.newInstance<Injected>()
    doLast {
        val sdkDirPath = injected.getAndroidSdkPath()
        injected.getSdkManagerFile()?.let { sdkManagerFile ->
            val yesInputStream = object : InputStream() {
                private val yesString = "y\n"
                private var counter = 0
                override fun read(): Int = yesString[counter % 2].also { counter++ }.code
            }
            injected.execOperations.exec {
                commandLine =
                    listOf(sdkManagerFile.absolutePath, "--list", "--sdk_root=$sdkDirPath")
                println("exec: ${this.commandLine.joinToString(separator = " ")}")
            }.apply { println("ExecResult: ${this.exitValue}") }
            injected.execOperations.exec {
                commandLine =
                    listOf(sdkManagerFile.absolutePath, "--licenses", "--sdk_root=$sdkDirPath")
                standardInput = yesInputStream
                println("exec: ${this.commandLine.joinToString(separator = " ")}")
            }.apply { println("ExecResult: ${this.exitValue}") }
        }
    }
}

tasks.register("devAll") {
    group = CI_GRADLE
    val injected = project.objects.newInstance<Injected>()
    doLast {
        injected.gradlew(
            "clean",
            "ktlintFormat",
            "ciBuildAndTest"
        )
    }
}

tasks.register("devEmulatorAll") {
    group = CI_GRADLE
    val injected = project.objects.newInstance<Injected>()
    doLast {
        injected.gradlew("ciSdkManagerLicenses")
        injected.gradlew("cleanManagedDevices", "--unused-only")
        (27..35).forEach { apiLevelIt ->
            val name =
                listOf(
                    "managedVirtualDevice",
                    apiLevelIt.toString()
                ).joinToString(separator = "")
            injected.gradlew(
                "${name}DebugAndroidTest",
                "-Pandroid.testInstrumentationRunnerArguments.class=" +
                    "siarhei.luskanau.managed.virtual.device.ExampleInstrumentedTest",
                "-Pandroid.testoptions.manageddevices.emulator.gpu=swiftshader_indirect",
                "--enable-display"
            )
        }
        injected.gradlew("cleanManagedDevices")
    }
}

abstract class Injected {

    @get:Inject abstract val fs: FileSystemOperations

    @get:Inject abstract val execOperations: ExecOperations

    @get:Inject abstract val projectLayout: ProjectLayout

    fun gradlew(vararg tasks: String, addToSystemProperties: Map<String, String>? = null) {
        execOperations.exec {
            commandLine = mutableListOf<String>().also { mutableArgs ->
                mutableArgs.add(
                    projectLayout.projectDirectory.file(
                        if (Os.isFamily(Os.FAMILY_WINDOWS)) "gradlew.bat" else "gradlew"
                    ).asFile.path
                )
                mutableArgs.addAll(tasks)
                addToSystemProperties?.toList()?.map { "-D${it.first}=${it.second}" }?.let {
                    mutableArgs.addAll(it)
                }
                mutableArgs.add("--stacktrace")
            }
            val sdkDirPath = Properties().apply {
                val propertiesFile = projectLayout.projectDirectory.file("local.properties").asFile
                if (propertiesFile.exists()) {
                    load(propertiesFile.inputStream())
                }
            }.getProperty("sdk.dir")
            if (sdkDirPath != null) {
                val platformToolsDir = "$sdkDirPath${File.separator}platform-tools"
                val pathEnvironment = System.getenv("PATH").orEmpty()
                if (!pathEnvironment.contains(platformToolsDir)) {
                    environment = environment.toMutableMap().apply {
                        put("PATH", "$platformToolsDir:$pathEnvironment")
                    }
                }
            }
            if (System.getenv("JAVA_HOME") == null) {
                System.getProperty("java.home")?.let { javaHome ->
                    environment = environment.toMutableMap().apply {
                        put("JAVA_HOME", javaHome)
                    }
                }
            }
            if (System.getenv("ANDROID_HOME") == null) {
                environment = environment.toMutableMap().apply {
                    put("ANDROID_HOME", sdkDirPath)
                }
            }
            println("commandLine: ${this.commandLine}")
        }.apply { println("ExecResult: ${this.exitValue}") }
    }

    fun runExec(
        commands: List<String>,
        inputStream: InputStream? = null,
        addToSystemEnvironment: Map<String, String>? = null
    ): String = object : ByteArrayOutputStream() {
        override fun write(p0: ByteArray, p1: Int, p2: Int) {
            print(String(p0, p1, p2))
            super.write(p0, p1, p2)
        }
    }.let { resultOutputStream ->
        execOperations.exec {
            commandLine = commands
            workingDir = projectLayout.projectDirectory.asFile
            environment = environment.toMutableMap().apply {
                System.getenv("HOME")?.also { put("HOME", it) }
                if (System.getenv("JAVA_HOME") == null) {
                    System.getProperty("java.home")?.let { javaHome ->
                        put("JAVA_HOME", javaHome)
                    }
                }
                addToSystemEnvironment?.also { putAll(addToSystemEnvironment) }
            }
            inputStream?.also { standardInput = inputStream }
            standardOutput = resultOutputStream
            println("commandLine: ${this.commandLine.joinToString(separator = " ")}")
        }.apply { println("ExecResult: $this") }
        String(resultOutputStream.toByteArray())
    }

    fun getAndroidSdkPath(): String? = Properties().apply {
        val propertiesFile = File(projectLayout.projectDirectory.asFile, "local.properties")
        if (propertiesFile.exists()) {
            load(propertiesFile.inputStream())
        }
    }.getProperty("sdk.dir").let { propertiesSdkDirPath ->
        (propertiesSdkDirPath ?: System.getenv("ANDROID_HOME"))
    }

    fun getSdkManagerFile(): File? = getAndroidSdkPath()?.let { sdkDirPath ->
        println("sdkDirPath: $sdkDirPath")
        val files = File(sdkDirPath).walk().filter { file ->
            file.path.contains("cmdline-tools") && file.path.endsWith("sdkmanager")
        }
        files.forEach { println("walk: ${it.absolutePath}") }
        val sdkmanagerFile = files.firstOrNull()
        println("sdkmanagerFile: $sdkmanagerFile")
        sdkmanagerFile
    }
}
