package sde.project

import sde.util.XDocument

actual class ProjectService : IProjectService {
    override suspend fun getFolderContents(path: String): List<ProjectItem> {
        TODO("Not yet implemented")
    }

    override suspend fun getFileContentsXDocument(path: String): XDocument {
        TODO("Not yet implemented")
    }

    override suspend fun getFileDefType(path: String): String {
        TODO("Not yet implemented")
    }
}