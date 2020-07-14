package sde.project

import kotlinx.serialization.Serializable

@Serializable
class Project
{
	var name: String = ""
	var projectRootPath: String = ""
	var defsFolder: String = ""

	var recentItems: List<String> = ArrayList()
}