package sde.project

import sde.util.getElement
import sde.util.parseXml
import sde.util.value
import java.io.File

object ProjectUtils
{
	fun readProjectRoot(path: String): Project {
		val file = File(path)

		val contents = file.readText()
		val xml = contents.parseXml()

		val name = xml.getElement("Name")?.value ?: "???"
		val defs = File(file.parent, xml.getElement("Definitions")?.value ?: "Definitions").canonicalPath

		val proj = Project()
		proj.name = name
		proj.projectRootPath = path
		proj.defsFolder = defs

		return proj
	}

	fun readFullProject(path: String): Project {
		val proj = readProjectRoot(path)

		return proj
	}
}