package convention.android.emulator

import java.io.File
import java.util.Properties
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.file.ProjectLayout
import org.gradle.kotlin.dsl.provideDelegate

internal class AndroidSdkConfig(private val projectLayout: ProjectLayout) {

    val sdkDirPath: String by lazy {
        readAndroidSdkFromLocalProperties()
            ?: System.getenv("ANDROID_HOME")
            ?: System.getenv("sdk.dir")
            ?: listOfNotNull(
                System.getProperty("user.home"),
                if (Os.isFamily(Os.FAMILY_MAC)) "Library" else null,
                "Android",
                if (Os.isFamily(Os.FAMILY_MAC)) "sdk" else "Sdk"
            ).joinToString(separator = File.separator)
    }

    init {
        writeAndroidSdkToLocalProperties()
    }

    val sdkmanager = sdkFile(
        "cmdline-tools",
        "bin",
        platformExecutable(name = "sdkmanager", ext = "bat")
    )
    val avdmanager = sdkFile(
        "cmdline-tools",
        AndroidSdkConst.CMDLINETOOLS_VERSION,
        "bin",
        platformExecutable(name = "avdmanager", ext = "bat")
    )
    val emulator = sdkFile("emulator", platformExecutable(name = "emulator"))
    val adb = sdkFile("platform-tools", platformExecutable(name = "adb"))

    fun printSdkPath() {
        println("sdk: ${sdkFile().exists()}: ${sdkFile()}")
        println("sdkmanager: ${sdkmanager.exists()}: $sdkmanager")
        println("avdmanager: ${avdmanager.exists()}: $avdmanager")
        println("emulator: ${emulator.exists()}: $emulator")
        println("adb: ${adb.exists()}: $adb")
    }

    private fun sdkFile(vararg path: String) = File(sdkDirPath, path.joinToString(File.separator))

    private fun readAndroidSdkFromLocalProperties(): String? = Properties().apply {
        val propertiesFile = File(projectLayout.projectDirectory.asFile, LOCAL_PROPERTIES_FILE_NAME)
        if (propertiesFile.exists()) {
            load(propertiesFile.inputStream())
        }
    }.getProperty(SDK_DIR)

    private fun writeAndroidSdkToLocalProperties() {
        val propertiesFile = File(projectLayout.projectDirectory.asFile, LOCAL_PROPERTIES_FILE_NAME)
        val properties = Properties().apply {
            if (propertiesFile.exists()) {
                load(propertiesFile.inputStream())
            }
        }
        properties[SDK_DIR] = sdkDirPath
        propertiesFile.outputStream().use { output ->
            properties.store(output, LOCAL_PROPERTIES_FILE_NAME)
        }
    }

    private fun platformExecutable(name: String, ext: String = "exe"): String =
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            "$name.$ext"
        } else {
            name
        }

    companion object {
        private const val LOCAL_PROPERTIES_FILE_NAME = "local.properties"
        private const val SDK_DIR = "sdk.dir"
    }
}
