package sde.project

import sde.Services
import sde.data.Project
import sde.util.RecentProject
import sde.util.Settings
import sde.util.XDocument
import sde.utils.parseXml
import sde.utils.toXDocument
import kotlin.js.Date

class ProjectDef
{
    var name: String = ""
    var projectRootPath: String = ""
    var defsFolder: String = ""

    companion object
    {
        suspend fun load(path: String): ProjectDef {
            val contents = Services.disk.loadFileString(path)
            val xml = contents.parseXml().toXDocument()
            return load(path, xml)
        }

        fun load(path: String, xml: XDocument): ProjectDef {
            val def = ProjectDef()
            def.name = xml.root.getElementValue("Name", "???")
            def.projectRootPath = path
            def.defsFolder = xml.root.getElementValue("Definitions", "")

            return def
        }
    }
}

fun Settings.removeRecentProject(path: String) {
    val itr = recentProjects.iterator()
    while (itr.hasNext()) {
        val proj = itr.next()

        if (proj.path == path) {
            itr.remove()
        }
    }
}

fun Settings.addRecentProject(path: String) {
    removeRecentProject(path)
    recentProjects.add(0, RecentProject(path, Date.now()))
}