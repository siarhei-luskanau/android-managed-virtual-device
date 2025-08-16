package convention.android.emulator

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
import java.util.zip.ZipFile
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.file.ProjectLayout

class AndroidSdkHelper(projectLayout: ProjectLayout, private val execWrapper: ExecWrapper) {

    private val androidSdkConfig = AndroidSdkConfig(projectLayout = projectLayout)
    private val addToSystemEnvironment = mapOf(
        "HOME" to System.getenv("HOME"),
        "ANDROID_SDK_HOME" to androidSdkConfig.sdkDirPath,
        "ANDROID_AVD_HOME" to "${projectLayout.projectDirectory.asFile}/.android/avd"
    ).filterValues { it.isNotEmpty() }

    fun setupAndroidCmdlineTools() {
        androidSdkConfig.printSdkPath()
        val commandlinetoolsUrl = commandLineToolsUrl(
            platform = when {
                Os.isFamily(Os.FAMILY_WINDOWS) -> "win"
                Os.isFamily(Os.FAMILY_MAC) -> "mac"
                else -> "linux"
            }
        )

        val commandlinetoolsPath = "${androidSdkConfig.sdkDirPath}/commandlinetools.zip"

        File(commandlinetoolsPath).parentFile.let {
            println("creating folder: $it")
            it.mkdirs()
        }

        println("downloading: $commandlinetoolsUrl")
        URL(commandlinetoolsUrl).openStream().use { input ->
            FileOutputStream(File(commandlinetoolsPath)).use { output ->
                input.copyTo(output)
            }
        }
        println("downloaded: $commandlinetoolsUrl")

        "${androidSdkConfig.sdkDirPath}/cmdline-tools/".also {
            println("deleting: $it")
            File(it).deleteRecursively()
            println("deleted: $it")
        }

        println("unzupping: $commandlinetoolsPath")
        val destDirectory = File(commandlinetoolsPath).parentFile
        ZipFile(commandlinetoolsPath).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                if (entry.isDirectory) {
                    File(destDirectory, entry.name).mkdirs()
                } else {
                    zip.getInputStream(entry).use { input ->
                        val outputFile = File(destDirectory, entry.name)
                        outputFile.parentFile.mkdirs()
                        outputFile.outputStream().use { input.copyTo(it) }
                        outputFile.setExecutable(true)
                    }
                }
            }
        }
        println("unzupped: $commandlinetoolsPath")

        println("deleting: $commandlinetoolsPath")
        File(commandlinetoolsPath).deleteRecursively()
        println("deleted: $commandlinetoolsPath")

        execWrapper.exec(
            commandLine = listOf(
                "${androidSdkConfig.sdkmanager}",
                "--licenses",
                "--sdk_root=${androidSdkConfig.sdkDirPath}"
            ),
            inputStream = yesInputStream()
        ).also { println("ExecOutput: $it") }

        execWrapper.exec(
            commandLine = listOf(
                androidSdkConfig.sdkmanager.absolutePath,
                "cmdline-tools;${AndroidSdkConst.CMDLINETOOLS_VERSION}",
                "--sdk_root=${androidSdkConfig.sdkDirPath}"
            ),
            inputStream = yesInputStream()
        ) // .also { println("ExecOutput: $it") }
    }

    fun setupAndroidSDK(avdName: String? = null) {
        androidSdkConfig.printSdkPath()
        execWrapper.exec(
            commandLine = listOf(
                androidSdkConfig.sdkmanager.absolutePath,
                "--update",
                "--sdk_root=${androidSdkConfig.sdkDirPath}"
            ),
            inputStream = yesInputStream()
        ).also { println("ExecOutput: $it") }

        execWrapper.exec(
            commandLine = listOf(
                androidSdkConfig.sdkmanager.absolutePath,
                "--licenses",
                "--sdk_root=${androidSdkConfig.sdkDirPath}"
            ),
            inputStream = yesInputStream()
        ).also { println("ExecOutput: $it") }

        execWrapper.exec(
            commandLine = mutableListOf(
                androidSdkConfig.sdkmanager.absolutePath,
                "tools",
                "platform-tools",
                "build-tools;${AndroidSdkConst.BUILD_TOOLS_VERSION}",
                "platforms;android-${AndroidSdkConst.PLATFORM}"
            ).apply {
                val emulatorConfig = ANDROID_EMULATORS.find { it.avdName == avdName }
                if (emulatorConfig != null) {
                    add("emulator")
                    add(emulatorConfig.sdkId)
                }
            }.apply {
                add("--sdk_root=${androidSdkConfig.sdkDirPath}")
            },
            inputStream = yesInputStream()
        ) // .also { println("ExecOutput: $it") }

        execWrapper.exec(
            commandLine = listOf(
                androidSdkConfig.sdkmanager.absolutePath,
                "--list",
                "--sdk_root=${androidSdkConfig.sdkDirPath}"
            ),
            inputStream = yesInputStream()
        ).also { println("ExecOutput: $it") }
    }

    fun setupAndroidEmulator(avdName: String) {
        androidSdkConfig.printSdkPath()
        ANDROID_EMULATORS.filter {
            if (avdName.isNotEmpty()) {
                it.avdName == avdName
            } else {
                true
            }
        }.forEach { emulatorConfig ->
            execWrapper.exec(
                commandLine = listOf(
                    androidSdkConfig.avdmanager.absolutePath,
                    "-v",
                    "create",
                    "avd",
                    "--force",
                    "-n",
                    emulatorConfig.avdName,
                    "--device",
                    emulatorConfig.deviceType,
                    "-k",
                    emulatorConfig.sdkId
                ),
                addToSystemEnvironment = addToSystemEnvironment
            ).also { println("ExecOutput: $it") }
        }

        execWrapper.exec(
            commandLine = listOf(androidSdkConfig.avdmanager.absolutePath, "-v", "list", "avd"),
            addToSystemEnvironment = addToSystemEnvironment
        ).also { println("ExecOutput: $it") }
        execWrapper.exec(
            commandLine = listOf(androidSdkConfig.emulator.absolutePath, "-list-avds"),
            addToSystemEnvironment = addToSystemEnvironment
        ).also { println("ExecOutput: $it") }
    }

    fun runAndroidEmulator(avdName: String) {
        androidSdkConfig.printSdkPath()
        execWrapper.exec(
            commandLine = listOf(androidSdkConfig.adb.absolutePath, "start-server")
        ).also { println("ExecOutput: $it") }

        val emulatorConfig = requireNotNull(ANDROID_EMULATORS.find { it.avdName == avdName })
            .also { println("EmulatorConfig: $it") }

        val commandArgs = mutableListOf(
            androidSdkConfig.emulator.absolutePath,
            "-avd",
            emulatorConfig.avdName,
            "-port",
            emulatorConfig.port
        )
        commandArgs.addAll(
            mutableListOf(
                "-accel",
                "auto",
                "-gpu",
                "swiftshader_indirect",
                "-dns-server",
                "8.8.8.8",
                "-no-audio",
                "-no-boot-anim",
                "-screen",
                "multi-touch",
                "-no-snapshot"
            ).also {
                if (true.toString().equals(other = System.getenv("CI"), ignoreCase = true)) {
                    it.add("-no-window")
                }
            }
        )
        execWrapper.exec(
            commandLine = commandArgs,
            addToSystemEnvironment = addToSystemEnvironment
        ).also { println("ExecOutput: $it") }
    }

    fun waitAndroidEmulator() {
        androidSdkConfig.printSdkPath()
        var isEmulatorFound = false
        for (i in 1..700) {
            Thread.sleep(1_000)
            var result: String? = null
            getDevicesList().also {
                if (it.isEmpty()) {
                    println("WaitAndroidEmulator: Wait $i: No emulators found!")
                }
            }.forEach { emulatorAttributes ->
                println("WaitAndroidEmulator: Wait $i: $emulatorAttributes")
                if (emulatorAttributes.contains("offline")) {
                    println("WaitAndroidEmulator: offline")
                } else {
                    result = execWrapper.exec(
                        commandLine = listOf(
                            androidSdkConfig.adb.absolutePath,
                            "-s",
                            emulatorAttributes.first(),
                            "wait-for-device",
                            "shell",
                            "getprop sys.boot_completed"
                        )
                    ).also { println("ExecOutput: $it") }.trim()
                }
            }
            println("sys.boot_completed = $result")
            if (result == "1") {
                println("Emulator booted")
                isEmulatorFound = true
                break
            }
        }
        if (isEmulatorFound.not()) {
            throw IllegalStateException("Emulator not found")
        }
    }

    fun killAndroidEmulator() {
        androidSdkConfig.printSdkPath()
        getDevicesList().forEach { emulatorAttributes ->
            println("KillAndroidEmulator: $emulatorAttributes")
            execWrapper.exec(
                commandLine = listOf(
                    androidSdkConfig.adb.absolutePath,
                    "-s",
                    emulatorAttributes.first(),
                    "emu",
                    "kill"
                )
            ).also { println("ExecOutput: $it") }
        }
    }

    fun deleteAndroidEmulator() {
        ANDROID_EMULATORS.forEach { emulatorConfig ->
            runCatching {
                execWrapper.exec(
                    commandLine = listOf(
                        androidSdkConfig.avdmanager.absolutePath,
                        "-v",
                        "delete",
                        "avd",
                        "-n",
                        emulatorConfig.avdName
                    ),
                    addToSystemEnvironment = addToSystemEnvironment
                ).also { println("ExecOutput: $it") }
            }
        }

        execWrapper.exec(
            commandLine = listOf(androidSdkConfig.avdmanager.absolutePath, "-v", "list", "avd"),
            addToSystemEnvironment = addToSystemEnvironment
        ).also { println("ExecOutput: $it") }
    }

    private fun getDevicesList(): List<List<String>> =
        execWrapper.exec(commandLine = listOf(androidSdkConfig.adb.absolutePath, "devices", "-l"))
            .let { devicesOutput ->
                println("ExecResult: $devicesOutput")
                String(devicesOutput.toByteArray())
                    .split("\n")
                    .filter { it.contains("emulator-") }
                    .map { deviceString ->
                        deviceString.split(" ")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                    }
            }

    companion object {
        private fun yesInputStream() = object : InputStream() {
            private val yesString = "y\n"
            private var counter = 0
            override fun read(): Int = yesString[counter % 2].also { counter++ }.code
        }

        private fun commandLineToolsUrl(platform: String): String = StringBuilder()
            .append("https://dl.google.com/android/repository/commandlinetools-")
            .append(platform)
            .append("-")
            .append(AndroidSdkConst.COMMANDLINETOOLS_VERSION)
            .append("_latest.zip")
            .toString()
    }
}
