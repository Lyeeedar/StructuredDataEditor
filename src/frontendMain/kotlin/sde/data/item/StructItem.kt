package sde.data.item

import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.html.*
import pl.treksoft.kvision.panel.DockPanel
import pl.treksoft.kvision.panel.Side
import pl.treksoft.kvision.require
import sde.data.DataDocument
import sde.data.definition.AbstractCompoundDefinition
import sde.data.definition.AbstractStructDefinition
import sde.data.definition.StructDefinition
import sde.ui.ImageButton
import sde.utils.hover

abstract class AbstractStructItem<D: AbstractStructDefinition<*, *>>(def: D, document: DataDocument) : AbstractCompoundDataItem<D>(def, document), IRemovable
{
	override fun getComponent(): Component
	{
		return DockPanel {
			if (!hasContent)
			{
				add(ImageButton(require("images/Add.png") as? String) {
					onClick {
						createContents()
						isExpanded = true
					}
				}, Side.LEFT)
			}

			span("this is the struct")
		}
	}

	override val canRemove: Boolean
		get() = def.nullable && children.size > 0

	var hasContent: Boolean by obs(children.size > 0)
		.raise(AbstractStructItem<*>::hasContent.name)
		.updatesComponent()
		.updatesDocument()
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