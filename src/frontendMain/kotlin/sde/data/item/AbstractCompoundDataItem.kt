package sde.data.item

import pl.treksoft.kvision.state.observableListOf
import sde.data.DataDocument
import sde.data.definition.AbstractCompoundDefinition

typealias CompoundDataItem = AbstractCompoundDataItem<*>

abstract class AbstractCompoundDataItem<D: AbstractCompoundDefinition<*, *>>(def: D, document: DataDocument) : AbstractDataItem<D>(def, document)
{
	val children = observableListOf<DataItem>()

	var isExpanded: Boolean by obs(false, CompoundDataItem::isExpanded.name)
		.updatesDocument()
		.get()

	override val description: String
		get() {
			val output = StringBuilder()

			val desc = def.description

			if (desc.isNotBlank()) {
				// resolve template
				val blocks = desc.split("}")
				for (block in blocks) {
					if (block.contains("{")) {
						val nameAndPath = block.split("{")
						output.append(nameAndPath[0])

						val item = getByPath(nameAndPath[1])
						if (item != null) {
							output.append("<span style=\"color:${item.def.textColour}\">")
							output.append(item.description)
							output.append("</span>")
						}
					} else {
						output.append(block)
					}
				}

			} else {
				// add attributes
				for (att in attributes) {
					if (att.name != "Name" && att.isDefault()) continue

					output.append("<span style=\"color:${att.def.textColour}\">")
					output.append(att.name)
					output.append("</span>")
					output.append("=")
					output.append(att.description)
				}

				// just do all children
				for (child in children) {
					val childDesc = child.description
					if (childDesc.isBlank()) continue

					if (output.isNotEmpty()) output.append(", ")

					output.append("<span style=\"color:${child.def.textColour}\">")
					output.append(childDesc)
					output.append("</span>")

					if (output.length > 800) {
						if (!output.endsWith("...")) {
							output.append("...")
						}

						break
					}
				}
			}

			return output.toString()
		}

	init
	{
		children.onUpdate.add {
			document.editor.update()

			for (item in children) {
				item.parent = this
			}

			raiseEvent(CompoundDataItem::children.name)
		}
	}
}