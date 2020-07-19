package sde.util

import node.fs.fs
import org.khronos.webgl.get
import pl.treksoft.kvision.electron.OpenDialogOptions
import pl.treksoft.kvision.electron.Remote
import pl.treksoft.kvision.electron.nodejs.Process
import pl.treksoft.kvision.require

external val process: Process

actual class DiskService : IDiskService {
    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    val remote: Remote = require("electron").remote as Remote

    override suspend fun loadFileString(path: String): String {
        return fs.readFileStringSync(path, "utf8")
    }

    override suspend fun loadFileBytes(path: String): List<Byte> {
        val buffer = fs.readFileBufferSync(path)
        return ByteArray(buffer.length) { i -> buffer[i] }.toList()
    }

    override suspend fun saveFileString(path: String, contents: String): Boolean {
        fs.writeFileSync(path, contents)
        return true
    }

    override suspend fun saveFileBytes(path: String, data: List<Byte>): Boolean {
        fs.writeFileSync(path, data.toByteArray())
        return true
    }

    override suspend fun getFolderContents(path: String): List<ProjectItem> {
        val output = ArrayList<ProjectItem>()

        val options = object {
            val encoding = "utf8"
            val withFileTypes = true
        }
        val rawContents: Array<Dirent> = fs.asDynamic().readdirSync(path, options) as Array<Dirent>

        for (child in rawContents) {
            val projItem = ProjectItem()
            projItem.path = path + "/" + child.name
            projItem.isDirectory = child.isDirectory()

            output.add(projItem)
        }

        return output
    }

    override suspend fun supportsNativeBrowser(): Boolean {
        return true
    }

    override suspend fun browseFile(fileTypes: String?, initialDirectory: String?): String {
        return remote.dialog.asDynamic().showOpenDialogSync(object : OpenDialogOptions {}.apply {
            properties = arrayOf("openFile")
        })[0] as String
    }

    override suspend fun browseFolder(initialDirectory: String?): String {
        return remote.dialog.asDynamic().showOpenDialogSync(object : OpenDialogOptions {}.apply {
            properties = arrayOf("openDirectory")
        })[0] as String
    }
}

interface Dirent {
    var name: String
    fun isDirectory(): Boolean
}