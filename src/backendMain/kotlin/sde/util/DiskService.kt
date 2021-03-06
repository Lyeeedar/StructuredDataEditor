package sde.util

import java.io.File
import java.nio.file.Files
import javax.swing.JFileChooser
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
            it.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        }?.canonicalPath?.replace('\\', '/') ?: ""
    }

    private fun chooseFile(init: ((JFileChooser)->Unit)? = null): File?
    {
        val frame = JFrame()
        frame.isVisible = true
        frame.extendedState = JFrame.ICONIFIED
        frame.extendedState = JFrame.NORMAL

        val fc = JFileChooser()

        init?.invoke(fc)

        if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(null)) {
            frame.isVisible = false
            frame.dispose()
            return fc.selectedFile
        }
        frame.isVisible = false
        frame.dispose()
        return null
    }
}