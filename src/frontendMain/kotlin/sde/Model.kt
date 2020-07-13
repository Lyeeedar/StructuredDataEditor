package sde

import sde.project.Project
import sde.project.ProjectService

object Model {

    private val pingService = PingService()
	private val projectService = ProjectService()

    suspend fun ping(message: String): String {
        return pingService.ping(message)
    }

	suspend fun getProject(): Project {
		return projectService.getProject()
	}

}
