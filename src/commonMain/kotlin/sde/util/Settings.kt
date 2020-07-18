package sde.util

import kotlinx.serialization.Serializable
import pl.treksoft.kvision.annotations.KVService

@KVService
interface ISettingsService {
    suspend fun loadSettings(): Settings
    suspend fun saveSettings(settings: Settings): Boolean
}

@Serializable
class Settings
{
    val recentProjects = ArrayList<RecentProject>()
}

@Serializable
class RecentProject() {
    var path: String = ""
    var lastOpened: Double = 0.0

    constructor(path: String, lastOpened: Double): this() {
        this.path = path
        this.lastOpened = lastOpened
    }
}