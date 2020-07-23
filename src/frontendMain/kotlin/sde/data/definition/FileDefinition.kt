package sde.data.definition

import sde.data.DataDocument
import sde.data.item.FileItem
import sde.data.item.StringItem
import sde.util.XElement

class FileDefinition : AbstractPrimitiveDataDefinition<FileDefinition, FileItem>()
{
	var maxLength: Int = -1

	override fun doParse(node: XElement)
	{
		maxLength = node.getAttributeValue("MaxLength", maxLength)
		default = node.getAttributeValue("Default", "")
	}

	override fun saveItemInstance(item: FileItem): XElement
	{
		return XElement(name, item.value)
	}

	override fun loadItemInstance(document: DataDocument, xml: XElement): FileItem
	{
		val item = FileItem(this, document)
		item.value = xml.value
		return item
	}

	override fun createItemInstance(document: DataDocument): FileItem
	{
		return FileItem(this, document)
	}
}