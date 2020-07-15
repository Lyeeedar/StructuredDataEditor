package sde.data.item

import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.html.Span
import sde.data.definition.StructDefinition

class StructItem(def: StructDefinition) : AbstractCompoundDataItem<StructDefinition>(def)
{
	override fun getComponent(): Component
	{
		return Span("Im a struct")
	}
}