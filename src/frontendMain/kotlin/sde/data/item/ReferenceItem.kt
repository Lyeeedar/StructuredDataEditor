package sde.data.item

import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.core.onClick
import pl.treksoft.kvision.form.select.SelectOptGroup
import pl.treksoft.kvision.form.select.selectInput
import pl.treksoft.kvision.panel.HPanel
import sde.data.DataDocument
import sde.data.definition.AbstractReferenceDefinition
import sde.data.definition.DataDefinition
import sde.data.definition.ReferenceDefinition
import sde.ui.imageButton
import sde.ui.textBlock

abstract class AbstractReferenceItem<D: AbstractReferenceDefinition<D, *>>(definition: D, document: DataDocument) : AbstractCompoundDataItem<D>(definition, document), IRemovable
{
    var selectedDefinition: DataDefinition by obs(def.contentsMap.values.first(), AbstractReferenceItem<*>::selectedDefinition.name)
            .get()

    var createdItem: DataItem? by obs<DataItem?>(null, AbstractReferenceItem<*>::createdItem.name)
            .undoable()
            .updatesDocument()
            .updatesComponent()
            .get()

    init {
        registerListener(AbstractReferenceItem<*>::createdItem.name) {
            val citem = createdItem
            if (citem == null) {
                name = def.name
            } else {
                name = "${def.name} (${citem.name})"
            }
        }
    }

    fun create() {
        val item = selectedDefinition.createItem(document)

        if (item is AbstractStructItem<*> && item.children.size == 0) {
            item.createContents()
        }

        setCreatedItem(item)
    }

    override val canRemove: Boolean
        get() = def.nullable && createdItem != null

    override fun remove() {
        val oldItem = createdItem ?: return

        document.undoRedoManager.applyDoUndo({
            attributes.clear()
            children.clear()

            document.undoRedoManager.disableUndoScope {
                createdItem = null
            }
        }, {
            attributes.addAll(oldItem.attributes)

            if (oldItem is CompoundDataItem) {
                children.addAll(oldItem.children)
            }

            document.undoRedoManager.disableUndoScope {
                createdItem = oldItem
            }
        }, "Remove ${def.name}")
    }

    fun setCreatedItem(item: DataItem) {
        val oldAttributes = attributes.toList()

        document.undoRedoManager.applyDoUndo({
            attributes.addAll(item.attributes)

            if (item is CompoundDataItem) {
                children.addAll(item.children)
            }

            document.undoRedoManager.disableUndoScope {
                createdItem = item
            }
        }, {
            attributes.clear()
            attributes.addAll(oldAttributes)

            children.clear()

            document.undoRedoManager.disableUndoScope {
                createdItem = null
            }
        }, "Create ${item.def.name} as ${def.name}")
    }

    override fun isDefaultValue(): Boolean {
        return createdItem == null
    }

    override fun getEditorComponent(): Component {
        if (createdItem != null) {
            return createdItem!!.getEditorComponentCached()
        } else {
            return HPanel {
                imageButton(pl.treksoft.kvision.require("images/Add.png") as? String) {
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
                    selectInput(value = selectedDefinition.name).apply {

                        for (category in def.contents.sortedBy { it.first })
                        {
                            val defs = category.second
                            if (defs.size > 0)
                            {
                                add(SelectOptGroup(category.first, defs.sortedBy { it.name }.map { it.name to it.name }))
                            }
                        }

                        subscribe {
                            this@AbstractReferenceItem.selectedDefinition = def.contentsMap[it]
                                    ?: def.contentsMap.values.first()
                        }
                        registerListener(AbstractReferenceItem<*>::selectedDefinition.name) {
                            this.value = this@AbstractReferenceItem.selectedDefinition.name
                        }
                    }
                }
            }
        }
    }
}

class ReferenceItem(definition: ReferenceDefinition, document: DataDocument) : AbstractReferenceItem<ReferenceDefinition>(definition, document)
{

}