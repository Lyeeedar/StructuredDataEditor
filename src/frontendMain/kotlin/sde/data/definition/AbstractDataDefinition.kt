package sde.data.definition

import org.w3c.dom.Element
import org.w3c.dom.Node
import sde.data.DataDocument
import sde.data.item.AbstractDataItem
import sde.data.item.DataItem
import sde.util.XAttribute
import sde.util.XDocument
import sde.util.XElement
import sde.utils.*

typealias DataDefinition = AbstractDataDefinition<*, *>
typealias DefinitionMap = HashMap<String, DataDefinition>

abstract class AbstractDataDefinition<D: AbstractDataDefinition<D, I>, I: AbstractDataItem<D>>
{
	val colours = mapOf(
		"Primitive" to "181,178,156",
		"Collection" to "156,171,181",
		"Struct" to "180,156,181"
	                   )

	lateinit var srcFile: String
	var fileColour: String = ""
	var fileIcon: String = ""

	lateinit var name: String
	var textColour: String = colours["Primitive"] ?: error("")
	lateinit var toolTip: String
	lateinit var visibleIf: String
	var skipIfDefault: Boolean = false
	var isGlobal = false
	var isDef = false

	val attributes = ArrayList<AbstractPrimitiveDataDefinition<*, *>>()
	var referenceMap = HashMap<String, DefinitionReference>()

	abstract fun children(): List<DataDefinition>

	fun registerReference(name: String, defName: String, category: String = "")
	{
		referenceMap[name] = DefinitionReference(name, defName, category)
	}

	fun registerReference(name: String, defNames: List<String>, category: String = "")
	{
		for (i in defNames.indices)
		{
			referenceMap[name+i] = DefinitionReference(name, defNames[i], category)
		}
	}

	fun getReference(name: String): DefinitionReference?
	{
		return referenceMap[name]
	}

	fun getReferences(name: String): List<DefinitionReference>
	{
		val output = ArrayList<DefinitionReference>()
		var i = 0

		while (true)
		{
			val ref = referenceMap[name+i] ?: break
			output.add(ref)
			i++
		}

		return output
	}

	var isResolved = false
	fun resolve(global: DefinitionMap, allDefs: HashMap<String, DefinitionMap>)
	{
		if (isResolved) return
		isResolved = true

		val local = allDefs[srcFile]!!

		for (ref in referenceMap)
		{
			val refHolder = ref.value
			val defName = refHolder.defName
			val def = local[defName] ?: global[defName] ?: throw DefinitionLoadException("Unable to resolve ${refHolder.name}: $defName")
			refHolder.definition = def

			def.resolve(global, allDefs)
		}

		for (child in children())
		{
			child.resolve(global, allDefs)
		}

		for (att in attributes)
		{
			att.resolve(global, allDefs)
		}

		postResolve()
	}

	protected open fun postResolve()
	{

	}

	fun parse(node: XElement)
	{
		name = node.getAttributeValue("Name", "???")
		textColour = node.getAttributeValue("TextColour", textColour)
		toolTip = node.getAttributeValue("ToolTip", "")
		visibleIf = node.getAttributeValue("VisibleIf", "")
		skipIfDefault = node.getAttributeValue("SkipIfDefault", skipIfDefault)
		isGlobal = node.getAttributeValue("IsGlobal", isGlobal)

		val attEl = node.getElement("Attributes")
		if (attEl != null)
		{
			for (att in attEl.children)
			{
				if (att !is XElement) continue

				val def = load(att, srcFile)

				if (def !is AbstractPrimitiveDataDefinition<*, *>)
				{
					throw DefinitionLoadException("Cannot put a non-primitive into attributes!")
				}

				attributes.add(def)
			}
		}

		doParse(node)
	}

	protected abstract fun doParse(node: XElement)

	fun createItem(document: DataDocument): I
	{
		val item = createItemInstance(document)

		for (att in attributes)
		{
			val attItem = att.createItem(document)
			item.attributes.add(attItem)
		}

		return item
	}

	fun loadItem(document: DataDocument, xml: XElement): I
	{
		val item = loadItemInstance(document, xml)

		for (att in attributes)
		{
			val attItem = att.loadItem(document, xml)
			item.attributes.add(attItem)
		}

		return item
	}

	fun saveItem(item: DataItem): XElement
	{
		val itemXml = saveItemInstance(item as I)

		for (att in item.attributes) {
			if (att.def.skipIfDefault && att.isDefault()) {
				continue
			}

			val attXmlEl = att.def.saveItem(att)
			val attXml = XAttribute()
			attXml.name = attXmlEl.name
			attXml.value = attXmlEl.value

			itemXml.attributes.add(attXml)
		}

		return itemXml
	}

	protected abstract fun saveItemInstance(item: I): XElement
	protected abstract fun loadItemInstance(document: DataDocument, xml: XElement): I
	protected abstract fun createItemInstance(document: DataDocument): I

	companion object
	{
		fun load(contents: XDocument, srcFile: String): DataDefinition
		{
			return load(contents.root, srcFile)
		}

		fun load(xml: XElement, srcFile: String): DataDefinition
		{
			var type = xml.getAttributeValue("meta:RefKey", "???").toUpperCase()
			if (type == "???")
			{
				throw DefinitionLoadException("The xml '${xml}' did not contain a meta:RefKey attribute")
			}

			var isDef = false
			if (type.endsWith("DEF")) {
				type = type.substring(0, type.length - "DEF".length)
				isDef = true
			}

			val def = when(type)
			{
				"BOOLEAN" -> BooleanDefinition()
				"COLOUR" -> ColourDefinition()
				"NUMBER" -> NumberDefinition()

				"STRUCT" -> StructDefinition()
				"GRAPHSTRUCT" -> GraphStructDefinition()

				else -> throw DefinitionLoadException("Unknown definition type $type")
			}
			def.srcFile = srcFile
			def.isDef = isDef

			def.parse(xml)

			return def
		}
	}
}

class DefinitionReference(val name: String, val defName: String, val category: String)
{
	var definition: DataDefinition? = null
}