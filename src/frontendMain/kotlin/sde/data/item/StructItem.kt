package sde.data.item

import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.html.Span
import pl.treksoft.kvision.html.button
import pl.treksoft.kvision.require
import sde.data.DataDocument
import sde.data.definition.AbstractCompoundDefinition
import sde.data.definition.AbstractStructDefinition
import sde.data.definition.StructDefinition

abstract class AbstractStructItem<D: AbstractStructDefinition<*, *>>(def: D, document: DataDocument) : AbstractCompoundDataItem<D>(def, document), IRemovable
{
	override fun getComponent(): Component
	{
		return Div {
			button("", icon = require("images/Add.png") as? String) {
				visible = !hasContent

				onClick {
					createContents()
					isExpanded = true
				}
			}
		}
	}

	override var canRemove by observable(def.nullable && children.size > 0)
	var hasContent by observable(children.size > 0, {old, new ->
		canRemove = def.nullable && new
	})

	override fun remove()
	{
		children.clear()
	}

	abstract fun createContents()

	init
	{
		children.onUpdate.add {
			hasContent = children.size > 0
		}
	}
}

class StructItem(def: StructDefinition, document: DataDocument) : AbstractStructItem<StructDefinition>(def, document)
{
	override fun createContents()
	{
		def.createContents(this, document)
	}

}