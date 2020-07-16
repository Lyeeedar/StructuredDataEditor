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

	override var canRemove: Boolean by obs(def.nullable && children.size > 0)
		.raise(AbstractStructItem<*>::canRemove.name)
		.updatesDocument()
		.updatedBy(AbstractStructItem<*>::hasContent.name) { canRemove = def.nullable && hasContent }
		.get()

	var hasContent: Boolean by obs(children.size > 0)
		.raise(AbstractStructItem<*>::hasContent.name)
		.updatesComponent()
		.updatedBy(CompoundDataItem::children.name) { hasContent = children.size > 0 }
		.get()

	override fun remove()
	{
		children.clear()
	}

	abstract fun createContents()
}

class StructItem(def: StructDefinition, document: DataDocument) : AbstractStructItem<StructDefinition>(def, document)
{
	override fun createContents()
	{
		def.createContents(this, document)
	}

}