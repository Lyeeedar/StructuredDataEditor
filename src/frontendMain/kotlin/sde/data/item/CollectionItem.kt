package sde.data.item

import io.kvision.core.Component
import io.kvision.core.CssSize
import io.kvision.core.UNIT
import io.kvision.core.onClick
import io.kvision.form.select.SelectOptGroup
import io.kvision.form.select.selectInput
import io.kvision.html.span
import io.kvision.panel.HPanel
import sde.data.DataDocument
import sde.data.definition.AbstractCollectionDefinition

import sde.data.definition.CollectionDefinition
import sde.data.definition.DataDefinition
import sde.ui.imageButton
import sde.ui.textBlock

abstract class AbstractCollectionItem<D: AbstractCollectionDefinition<D, *>>(definition: D, document: DataDocument) : AbstractCompoundDataItem<D>(definition, document)
{
	protected var selectedDefinition: DataDefinition by obs(def.contentsMap.values.first(), AbstractCollectionItem<*>::selectedDefinition.name)
		.get()

	init
	{
		children.onUpdate.add {
			for (child in children) {
				if (def.contentsMap.containsKey(child.name)) {
					child.isCollectionChild = true
				}
			}

			if (def.childrenAreUnique) {
				val validDefs = getValidChildDefinitions().toSet()
				if (validDefs.size > 0 && !validDefs.contains(selectedDefinition)) {
					selectedDefinition = validDefs.first()
				}

				if (isVisible())
				{
					forceEditorComponentRefresh()
				}
			}

			name = "${def.name} (${children.filter { it !is CommentItem }.size})"
		}
		name = "${def.name} (${children.filter { it !is CommentItem }.size})"
	}

	override fun isDefaultValue(): Boolean
	{
		return children.size == def.additionalDefs.size
	}

	var canAdd: Boolean by obs(getValidChildDefinitions().count() > 0, AbstractCollectionItem<*>::canAdd.name)
		.updatesComponent()
		.updatedBy(CompoundDataItem::children.name) { canAdd = getValidChildDefinitions().count() > 0 }
		.get()

	protected fun getValidChildDefinitions(): Sequence<DataDefinition> {
		return sequence {

			if (children.size < def.maxCount + def.additionalDefs.size) {
				for (child in def.contentsMap.values) {
					if (def.childrenAreUnique) {
						if (!children.any { it.def == child }) {
							yield(child)
						}
					} else {
						yield(child)
					}
				}
			}
		}
	}

	fun create(): DataItem {
		return create(selectedDefinition)
	}

	fun create(def: DataDefinition): DataItem {
		val newChild = def.createItem(document)
		if (newChild is AbstractStructItem<*> && newChild.children.size == 0) {
			newChild.createContents()
		}

		document.undoRedoManager.applyDoUndo({ children.add(newChild) }, { children.remove(newChild) }, "Add ${newChild.name} to ${def.name}")

		return newChild
	}

	override fun getEditorComponent(): Component
	{
		return HPanel {
			imageButton(io.kvision.require("images/Add.png") as? String) {
				visible = canAdd
				marginRight = CssSize(5, UNIT.px)

				onClick {

					create()

					isExpanded = true
				}
			}

			if (def.contentsMap.size == 1) {
				textBlock(selectedDefinition.name) {
					opacity = 0.4

				}
			} else {
				val validDefs = getValidChildDefinitions().toSet()
				if (validDefs.size > 0)
				{
					selectInput(value = selectedDefinition.name).apply {

						for (category in def.contents.sortedBy { it.first })
						{
							val defs = category.second.filter { validDefs.contains(it) }
							if (defs.size > 0)
							{
								add(SelectOptGroup(category.first, defs.sortedBy { it.name }.map { it.name to it.name }))
							}
						}

						subscribe {
							this@AbstractCollectionItem.selectedDefinition = def.contentsMap[it]
							                                                 ?: def.contentsMap.values.first()
						}
						registerListener(AbstractCollectionItem<*>::selectedDefinition.name) {
							this.value = this@AbstractCollectionItem.selectedDefinition.name
						}
					}
				}
			}
		}
	}
}

class CollectionItem(definition: CollectionDefinition, document: DataDocument) : AbstractCollectionItem<CollectionDefinition>(definition, document)
{

}