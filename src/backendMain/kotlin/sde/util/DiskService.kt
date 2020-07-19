package sde.util

import java.io.File
import java.nio.file.Files
import javax.swing.JFileChooser
import javax.swing.JFrame

actual class DiskService : IDiskService {
    override suspend fun loadFileString(path: String): String {
        return File(path).readText()
    }

    override suspend fun loadFileBytes(path: String): List<Byte> {
        return File(path).readBytes().toList()
    }

    override suspend fun saveFileString(path: String, contents: String): Boolean {
        val file = File(path)

        val parent = file.parentFile
        if (!parent.exists())
        {
            parent.mkdirs()
        }

        File(path).writeText(contents)
        return true
    }

    override suspend fun saveFileBytes(path: String, data: List<Byte>): Boolean {
        val file = File(path)

        val parent = file.parentFile
        if (!parent.exists())
        {
            parent.mkdirs()
        }

        File(path).writeBytes(data.toByteArray())
        return true
    }

    override suspend fun getFolderContents(path: String): List<ProjectItem> {
        val output = ArrayList<ProjectItem>()

        val folder = File(path)

        for (child in Files.list(folder.toPath())) {
            val item = child.toFile()

            val projItem = ProjectItem()
            projItem.path = item.canonicalPath
            projItem.isDirectory = item.isDirectory

            output.add(projItem)
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
        }!!.canonicalPath.replace('\\', '/')
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