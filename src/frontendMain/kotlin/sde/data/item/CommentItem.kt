package sde.data.item

import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.html.Span
import pl.treksoft.kvision.panel.DockPanel
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

		return DockPanel {
			marginLeft = CssSize(depth, UNIT.px)

			add(Div {
				height = CssSize(1, UNIT.px)
				width = CssSize(100, UNIT.perc)
			}, Side.LEFT)

			add(Div {
				height = CssSize(1, UNIT.px)
				width = CssSize(100, UNIT.perc)
			}, Side.RIGHT)

			add(Span(value))
		}
	}

	override fun getEditorComponent(): Component
	{
		throw NotImplementedError()
	}
}