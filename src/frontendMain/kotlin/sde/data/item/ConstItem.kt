package sde.data.item

import io.kvision.core.Component
import io.kvision.html.Div
import sde.data.DataDocument
import sde.data.definition.ConstDefinition

class ConstItem(definition: ConstDefinition, document: DataDocument) : AbstractDataItem<ConstDefinition>(definition, document)
{
	override val description: String
		get() = ""

	override fun isDefaultValue(): Boolean = true

	override fun getEditorComponent(): Component
	{
		return Div()
	}
}