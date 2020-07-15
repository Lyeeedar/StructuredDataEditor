package sde.project

import kotlinx.serialization.Serializable

@Serializable
class ProjectDef
{
	var name: String = ""
	var projectRootPath: String = ""
	var defsFolder: String = ""

	var recentItems: List<String> = ArrayList()
}