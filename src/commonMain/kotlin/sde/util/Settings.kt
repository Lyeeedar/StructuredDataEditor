package sde.util

import kotlinx.serialization.Serializable

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