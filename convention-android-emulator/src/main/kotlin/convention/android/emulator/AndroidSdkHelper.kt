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
