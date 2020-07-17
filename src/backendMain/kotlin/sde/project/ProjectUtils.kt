package sde.project

import sde.util.parseXml
import sde.util.toXDocument
import java.io.File

object ProjectUtils
{
	fun readProjectRoot(path: String): ProjectDef {
		val file = File(path)

		val contents = file.readText()
		val xml = contents.parseXml().toXDocument()

		val name = xml.root.getElement("Name")?.value ?: "???"
		val defs = File(file.parent, xml.root.getElement("Definitions")?.value ?: "Definitions").canonicalPath

		val proj = ProjectDef()
		proj.name = name
		proj.projectRootPath = path
		proj.defsFolder = defs

		return proj
	}

	fun readFullProject(path: String): ProjectDef {
		val proj = readProjectRoot(path)

		return proj
	}
}