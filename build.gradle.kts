@file:Suppress("PropertyName")

import convention.android.emulator.AndroidSdkHelper
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.apply
import org.gradle.kotlin.dsl.newInstance

plugins {
    id("androidEmulatorConvention")
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.detekt)
}

allprojects {
    apply(from = "$rootDir/ktlint.gradle")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    detekt {
        parallel = true
        ignoreFailures = false
    }
}

val CI_GRADLE = "CI_GRADLE"

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

abstract class Injected {

    @get:Inject abstract val execOperations: ExecOperations

    @get:Inject abstract val projectLayout: ProjectLayout

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
}
