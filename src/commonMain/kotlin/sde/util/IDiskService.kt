package sde.util

import kotlinx.serialization.Serializable
import pl.treksoft.kvision.annotations.KVService

@KVService
interface IDiskService {
	suspend fun fileExists(path: String): Boolean
    suspend fun loadFileString(path: String): String
    suspend fun loadFileBytes(path: String): List<Byte>
    suspend fun saveFileString(path: String, contents: String): Boolean
    suspend fun saveFileBytes(path: String, data: List<Byte>): Boolean
    suspend fun getFolderContents(path: String): List<ProjectItem>
    suspend fun supportsNativeBrowser(): Boolean
    suspend fun browseFile(fileTypes: String? = null, initialDirectory: String? = null): String
    suspend fun browseFolder(initialDirectory: String? = null): String
}

@Serializable
class ProjectItem
{
    var path: String = ""
    var isDirectory: Boolean = false
}