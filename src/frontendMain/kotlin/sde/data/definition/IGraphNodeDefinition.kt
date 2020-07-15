package sde.data.definition

interface IGraphNodeDefinition
{
	var allowReferenceLinks: Boolean
	var allowCircularLinks: Boolean
	var flattenData: Boolean
	var nodeStoreName: String
	var background: String
}