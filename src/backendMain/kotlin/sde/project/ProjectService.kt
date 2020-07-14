package sde.project

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

	override suspend fun getFileContents(path: String): String
	{
		val file = File(path)
		return file.readText()
	}
}