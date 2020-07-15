package sde.project

import kotlinx.serialization.Serializable
import pl.treksoft.kvision.annotations.KVService

@KVService
interface IProjectService {
	suspend fun getFolderContents(path: String): List<ProjectItem>
	suspend fun getFileContents(path: String): String
	suspend fun getFileDefType(path: String): String
}

@Serializable
class ProjectItem
{
	var path: String = ""
	var isDirectory: Boolean = false
}