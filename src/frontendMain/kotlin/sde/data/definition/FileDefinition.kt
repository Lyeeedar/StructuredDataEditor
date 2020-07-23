package sde.data.definition

import sde.data.DataDocument
import sde.data.item.FileItem
import sde.data.item.StringItem
import sde.util.XElement

class FileDefinition : AbstractPrimitiveDataDefinition<FileDefinition, FileItem>()
{
	var stripExtension = false
	var basePath = ""
	var resourceDef: DataDefinition? = null
	val allowedFileTypes = ArrayList<String>()
	var relativeToThis = false

	override fun doParse(node: XElement)
	{
		default = node.getAttributeValue("Default", "")
		stripExtension = node.getAttributeValue("StripExtension", stripExtension)
		basePath = node.getAttributeValue("BasePath", basePath)

		val resourceType = node.getAttributeValue("ResourceType", "")
		if (resourceType.isNotBlank()) {
			registerReference("ResourceDef", resourceType)
		}

		val fileTypes = node.getAttributeValue("AllowedFileTypes", "")
		if (fileTypes.isNotBlank()) {
			allowedFileTypes.addAll(fileTypes.split(','))
		}

		relativeToThis = node.getAttributeValue("RelativeToThis", relativeToThis)
	}

	override fun postResolve() {
		val def = getReference<DataDefinition>("ResourceDef") ?: return
		resourceDef = def
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