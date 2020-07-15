package sde

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import pl.treksoft.kvision.annotations.KVService
import pl.treksoft.kvision.types.LocalDateTime
import sde.project.ProjectDef

@KVService
interface IStartPageService {
	suspend fun getRecentProjects(): List<RecentProject>
	suspend fun removeRecentProject(path: String): Boolean

	suspend fun openProject(path: String): ProjectDef
	suspend fun browseExistingProject(): ProjectDef
	suspend fun createNewProject(config: NewProjectConfig): ProjectDef

	suspend fun browseFolder(): String
}

@Serializable
class NewProjectConfig(var rootFolder: String, var defsFolder: String, var name: String)

@Serializable
class RecentProject() {
	var path: String = ""
	var name: String = ""

	@ContextualSerialization
	lateinit var lastOpened: LocalDateTime

	constructor(path: String, name: String, lastOpened: LocalDateTime): this() {
		this.path = path
		this.name = name
		this.lastOpened = lastOpened
	}
}