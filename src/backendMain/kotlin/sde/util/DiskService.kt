package sde.util

import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.nio.file.Files
import javax.swing.JFrame

actual class DiskService : IDiskService {

	override suspend fun fileExists(path: String): Boolean
	{
		return File(path).exists()
	}

	override suspend fun loadFileString(path: String): String {
		if (!fileExists(path)) return ""
        return File(path).readText()
    }

    override suspend fun loadFileBytes(path: String): List<Byte> {
	    if (!fileExists(path)) return ArrayList()
        return File(path).readBytes().toList()
    }

    override suspend fun saveFileString(path: String, contents: String): Boolean {
	    try {
		    val file = File(path)

		    val parent = file.parentFile
		    if (!parent.exists())
		    {
			    parent.mkdirs()
		    }

		    File(path).writeText(contents)
		    return true
	    } catch (ex: Exception) {
		    return false
	    }
    }

    override suspend fun saveFileBytes(path: String, data: List<Byte>): Boolean {
	    try {
		    val file = File(path)

		    val parent = file.parentFile
		    if (!parent.exists())
		    {
			    parent.mkdirs()
		    }

		    File(path).writeBytes(data.toByteArray())
		    return true
	    } catch (ex: Exception) {
		    return false
	    }
    }

    override suspend fun getFolderContents(path: String): List<ProjectItem> {
        val output = ArrayList<ProjectItem>()

        val folder = File(path)

	    if (folder.exists())
	    {
		    for (child in Files.list(folder.toPath()))
		    {
			    val item = child.toFile()

			    val projItem = ProjectItem()
			    projItem.path = item.canonicalPath
			    projItem.isDirectory = item.isDirectory

			    output.add(projItem)
		    }
	    }

        return output
    }

    override suspend fun supportsNativeBrowser(): Boolean {
        return true
    }

    override suspend fun browseFile(fileTypes: String?, initialDirectory: String?): String {
        return chooseFile()?.canonicalPath?.replace('\\', '/') ?: ""
    }

    override suspend fun browseFolder(initialDirectory: String?): String {
        return chooseFile {
            //it.
        }?.canonicalPath?.replace('\\', '/') ?: ""
    }

    private fun chooseFile(init: ((FileDialog)->Unit)? = null): File?
    {
	    val dialog = FileDialog(null as Frame?, "Choose a file", FileDialog.LOAD)
	    dialog.isVisible = true
	    dialog.isAlwaysOnTop = true
	    dialog.toFront()
	    dialog.requestFocus()

        init?.invoke(dialog)

        if (dialog.file != null) {
			val file = dialog.files[0]
			dialog.dispose()
            return file
        }
	    dialog.dispose()
        return null
    }
}