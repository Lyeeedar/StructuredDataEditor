package sde.utils

import sde.Services
import sde.util.RecentProject
import sde.util.Settings

suspend fun updateSettings(body: (Settings)->Unit) {
    val settings = getSettings()
    body(settings)
    Services.settings.saveSettings(settings)
}

suspend fun getSettings(): Settings = Services.settings.loadSettings()

suspend fun RecentProject.getProjectName(): String {
    val contents = Services.disk.loadFileString(this.path)
    val xml = contents.parseXml().toXDocument()
    return xml.root.getElementValue("Name", "???")
}