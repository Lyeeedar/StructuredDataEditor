package sde.data.item

import sde.data.DataDocument
import sde.data.definition.GraphStructDefinition

class GraphStructItem(def: GraphStructDefinition, document: DataDocument) : AbstractStructItem<GraphStructDefinition>(def, document)
{
	override fun createContents()
	{
		def.createContents(this, document)
	}
}