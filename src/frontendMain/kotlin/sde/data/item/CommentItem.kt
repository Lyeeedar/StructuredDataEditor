package sde.data.item

import io.kvision.core.*
import io.kvision.html.Align
import io.kvision.html.Div
import io.kvision.html.Span
import io.kvision.panel.DockPanel
import io.kvision.panel.GridPanel
import io.kvision.panel.Side
import sde.data.DataDocument
import sde.data.definition.CommentDefinition
import sde.ui.TextBlock

class CommentItem(document: DataDocument, initialValue: String = "") : AbstractDataItem<CommentDefinition>(CommentDefinition(), document)
{
	var value: String by obs(initialValue, CommentItem::value.name)
		.undoable()
		.get()

	override val description: String
		get() = ""

	override fun isDefaultValue(): Boolean
	{
		return false
	}

	override fun getEditorRow(sensibleHeaderWidth: Int): Component
	{
		val depth = depth * 14 + 14

		return GridPanel(templateColumns = "1fr auto 1fr") {
			marginLeft = CssSize(depth, UNIT.px)

			add(TextBlock(value), 2)
		}
	}

	override fun getEditorComponent(): Component
	{
		throw NotImplementedError()
	}
}