package sde.data.item

import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.core.onClick
import pl.treksoft.kvision.form.select.SelectOptGroup
import pl.treksoft.kvision.form.select.selectInput
import pl.treksoft.kvision.panel.HPanel
import sde.data.DataDocument
import sde.data.definition.AbstractCollectionDefinition

import sde.data.definition.CollectionDefinition
import sde.data.definition.DataDefinition
import sde.ui.imageButton

abstract class AbstractCollectionItem<D: AbstractCollectionDefinition<*, *>>(definition: D, document: DataDocument) : AbstractCompoundDataItem<D>(definition, document)
{
	protected var selectedDefinition: DataDefinition by obs(def.contentsMap.values.first(), AbstractCollectionItem<*>::selectedDefinition.name)
		.get()

	override fun isDefaultValue(): Boolean
	{
		return children.size == def.additionalDefs.size
	}

	var canAdd: Boolean by obs(children.size < def.maxCount, AbstractCollectionItem<*>::canAdd.name)
		.updatesComponent()
		.updatedBy(CompoundDataItem::children.name) { canAdd = children.size < def.maxCount }
		.get()

	override fun getEditorComponent(): Component
	{
		return HPanel {
			imageButton(pl.treksoft.kvision.require("images/Add.png") as? String) {
				visible = canAdd

				onClick {

					val newChild = selectedDefinition.createItem(document)

					document.undoRedoManager.applyDoUndo({ children.add(newChild) }, { children.remove(newChild) }, "Add ${newChild.name} to $name")

					isExpanded = true
				}
			}

			selectInput(value = selectedDefinition.name).apply {
				for (category in def.contents.sortedBy { it.first }) {
					val defs = category.second
					add(SelectOptGroup(category.first, defs.sortedBy { it.name }.map { it.name to it.name }))
				}

				subscribe {
					this@AbstractCollectionItem.selectedDefinition = def.contentsMap[it] ?: def.contentsMap.values.first()
				}
				registerListener(AbstractCollectionItem<*>::selectedDefinition.name) {
					this.value = this@AbstractCollectionItem.selectedDefinition.name
				}
			}
		}
	}
}

class CollectionItem(definition: CollectionDefinition, document: DataDocument) : AbstractCollectionItem<CollectionDefinition>(definition, document)
{

}