package sde.util

import node.fs.fs
import org.khronos.webgl.get
import org.w3c.files.Blob
import org.w3c.files.FileReaderSync
import pl.treksoft.kvision.electron.OpenDialogOptions
import pl.treksoft.kvision.electron.Remote
import pl.treksoft.kvision.electron.nodejs.Process
import pl.treksoft.kvision.require
import pl.treksoft.kvision.toast.Toast
import pl.treksoft.kvision.toast.ToastOptions
import pl.treksoft.kvision.toast.ToastPosition

external val process: Process

actual class DiskService : IDiskService {
    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    val remote: Remote = require("electron").remote as Remote

	override suspend fun fileExists(path: String): Boolean
	{
		return fs.existsSync(path)
	}

	override suspend fun loadFileString(path: String): String {
	    try {
		    return fs.readFileStringSync(path, "utf8")
	    } catch (ex: Throwable) {
		    return ""
	    }
    }

    override suspend fun loadFileBytes(path: String): List<Byte> {
	    try {
		    val buffer = fs.readFileBufferSync(path)
		    return ByteArray(buffer.length) { i -> buffer[i] }.toList()
	    } catch (ex: Throwable) {
		    return listOf()
	    }
    }

    override suspend fun saveFileString(path: String, contents: String): Boolean {
	    try {
		    fs.writeFileSync(path, contents)
		    return true
	    } catch (ex: Throwable) {
		    return false
	    }
    }

    override suspend fun saveFileBytes(path: String, data: List<Byte>): Boolean {
	    try {
		    fs.writeFileSync(path, data.toByteArray())
		    return true
	    } catch (ex: Throwable) {
		    return false
	    }
    }

    override suspend fun getFolderContents(path: String): List<ProjectItem> {
        val output = ArrayList<ProjectItem>()

	    try {
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
	    } catch (ex: Throwable) {
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