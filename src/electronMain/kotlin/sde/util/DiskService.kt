package sde.util

actual class DiskService : IDiskService {
    override suspend fun loadFileString(path: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun saveFileString(path: String, contents: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getFolderContents(path: String): List<ProjectItem> {
        TODO("Not yet implemented")
    }

    override suspend fun supportsNativeBrowser(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun browseFile(fileTypes: String?, initialDirectory: String?): String {
        TODO("Not yet implemented")
    }

    override suspend fun browseFolder(initialDirectory: String?): String {
        TODO("Not yet implemented")
    }
}