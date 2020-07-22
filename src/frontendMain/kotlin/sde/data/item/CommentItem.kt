package sde.data.item

import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.html.Align
import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.html.Span
import pl.treksoft.kvision.panel.DockPanel
import pl.treksoft.kvision.panel.GridPanel
import pl.treksoft.kvision.panel.Side
import sde.data.DataDocument
import sde.data.definition.CommentDefinition

class CommentItem(document: DataDocument, initialValue: String = "") : AbstractDataItem<CommentDefinition>(CommentDefinition(), document)
{
	var value: String by obs(initialValue, CommentItem::value.name)
		.undoable()
		.get()

	override fun isDefaultValue(): Boolean
	{
		return false
	}

	override fun getEditorRow(sensibleHeaderWidth: Int): Component
	{
		val depth = depth * 14 + 14

		return GridPanel(templateColumns = "1fr auto 1fr") {
			marginLeft = CssSize(depth, UNIT.px)

			add(Span(value), 2)
		}
	}

	override fun getEditorComponent(): Component
	{
		throw NotImplementedError()
	}
}