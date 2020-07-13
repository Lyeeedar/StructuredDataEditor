package sde

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import pl.treksoft.kvision.annotations.KVService
import pl.treksoft.kvision.types.LocalDateTime
import sde.project.Project

@KVService
interface IStartPageService {
	suspend fun getRecentProjects(): List<RecentProject>
	suspend fun removeRecentProject(path: String): Boolean

	suspend fun openProject(path: String): Project
	suspend fun browseExistingProject(): Project
	suspend fun createNewProject(): Project
}

@Serializable
class RecentProject() {
	var path: String = ""

	@ContextualSerialization
	lateinit var lastOpened: LocalDateTime

	constructor(path: String, lastOpened: LocalDateTime): this() {
		this.path = path
		this.lastOpened = lastOpened
	}
}