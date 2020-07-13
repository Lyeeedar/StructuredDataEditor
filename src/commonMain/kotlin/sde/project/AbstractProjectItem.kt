package sde.project

import kotlinx.serialization.Serializable

@Serializable
abstract class AbstractProjectItem
{
	var name: String = ""
	var parent: AbstractProjectItem? = null
	var depth: Int = 0
}