package sde

import sde.project.ProjectDef

actual class StartPageService : IStartPageService {
    override suspend fun getRecentProjects(): List<RecentProject> {
        TODO("Not yet implemented")
    }

    override suspend fun removeRecentProject(path: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun openProject(path: String): ProjectDef {
        TODO("Not yet implemented")
    }

    override suspend fun browseExistingProject(): ProjectDef {
        TODO("Not yet implemented")
    }

    override suspend fun createNewProject(config: NewProjectConfig): ProjectDef {
        TODO("Not yet implemented")
    }

    override suspend fun browseFolder(): String {
        TODO("Not yet implemented")
    }
}