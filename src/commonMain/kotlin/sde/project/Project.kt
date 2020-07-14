package sde.project

import kotlinx.serialization.Serializable
import sde.data.definition.AbstractDataDefinition

@Serializable
class Project
{
	var name: String = ""
	var projectRootPath: String = ""
	var defsFolder: String = ""

	var definitions: List<AbstractDataDefinition> = ArrayList()
	var projectItems: List<AbstractProjectItem> = ArrayList()
	var recentItems: List<String> = ArrayList()
}