package sde.project

import kotlinx.serialization.Serializable
import sde.data.definition.AbstractDataDefinition

@Serializable
class Project
{
	var path: String = ""
	var definitions: List<AbstractDataDefinition> = ArrayList()
	var projectItems: List<AbstractProjectItem> = ArrayList()
	var recentItems: List<String> = ArrayList()
}