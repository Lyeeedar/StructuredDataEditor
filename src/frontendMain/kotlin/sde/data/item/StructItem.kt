package sde.data.item

import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.html.*
import pl.treksoft.kvision.panel.DockPanel
import pl.treksoft.kvision.panel.HPanel
import pl.treksoft.kvision.panel.Side
import pl.treksoft.kvision.require
import sde.data.DataDocument
import sde.data.definition.AbstractCompoundDefinition
import sde.data.definition.AbstractStructDefinition
import sde.data.definition.StructDefinition
import sde.ui.ImageButton
import sde.ui.imageButton
import sde.ui.textBlock
import sde.utils.hover

abstract class AbstractStructItem<D: AbstractStructDefinition<*, *>>(def: D, document: DataDocument) : AbstractCompoundDataItem<D>(def, document), IRemovable
{
	override fun getEditorComponent(): Component
	{
		return HPanel {
			imageButton(require("images/Add.png") as? String) {
				visible = !hasContent

				onClick {
					createContents()

					val contents = children.toList()
					children.clear()

					document.undoRedoManager.applyDoUndo({ children.addAll(contents) }, { children.clear() }, "Create $name")

					isExpanded = true
				}
			}

			textBlock(description, rich = true) {
				marginLeft = CssSize(5, UNIT.px)

				registerListener(DataItem::description.name) {
					content = description
				}
			}
		}
	}

	override val canRemove: Boolean
		get() = def.nullable && children.size > 0

	var hasContent: Boolean by obs(children.size > 0, AbstractStructItem<*>::hasContent.name)
		.updatesComponent()
		.updatesDocument()
		.updatedBy(CompoundDataItem::children.name) { hasContent = children.size > 0 }
		.get()

	override fun isDefaultValue(): Boolean
	{
		return !hasContent
	}

	override fun remove()
	{
		val contents = children.toList()
		document.undoRedoManager.applyDoUndo({ children.clear() }, { children.addAll(contents) }, "Remove $name")
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