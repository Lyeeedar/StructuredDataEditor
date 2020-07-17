package sde.project

import sde.util.XDocument
import sde.util.parseXml
import sde.util.toXDocument
import java.io.File
import java.nio.file.Files

actual class ProjectService : IProjectService
{
	override suspend fun getFolderContents(path: String): List<ProjectItem>
	{
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

	override suspend fun getFileContentsXDocument(path: String): XDocument
	{
		val file = File(path)
		return file.readText().parseXml().toXDocument()
	}

	override suspend fun getFileDefType(path: String): String
	{
		val xml = getFileContentsXDocument(path)
		return xml.root.name
	}
}