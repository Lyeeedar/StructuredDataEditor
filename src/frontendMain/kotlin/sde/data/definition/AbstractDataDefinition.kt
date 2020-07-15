package sde.data.definition

import org.w3c.dom.Element
import org.w3c.dom.Node
import sde.data.item.AbstractDataItem
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

	lateinit var name: String
	var textColour: String = colours["Primitive"] ?: error("")
	lateinit var toolTip: String
	lateinit var visibleIf: String
	var skipIfDefault: Boolean = false
	var isGlobal = false

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

	open fun postResolve()
	{

	}

	fun parse(node: Element)
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
			for (att in attEl.childNodes.elements())
			{
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

	abstract fun doParse(node: Element)

	fun createItem(): I
	{
		val item = createItemInstance()

		for (att in attributes)
		{
			val attItem = att.createItem()
			item.attributes.add(attItem)
		}

		return item
	}

	abstract fun createItemInstance(): I

	companion object
	{
		fun load(contents: String, srcFile: String): DataDefinition
		{
			val xml = contents.parseXml()

			return load(xml.childNodes.elements().first(), srcFile)
		}

		fun load(xml: Element, srcFile: String): DataDefinition
		{
			var type = xml.getAttributeValue("meta:RefKey", "???").toUpperCase()
			if (type == "???")
			{
				throw DefinitionLoadException("The xml '${xml.serializeXml()}' did not contain a meta:RefKey attribute")
			}

			if (type.endsWith("DEF")) {
				type = type.substring(0, type.length - "DEF".length)
			}

			val def = when(type)
			{
				"NUMBER" -> NumberDefinition()

				"STRUCT" -> StructDefinition()
				"GRAPHSTRUCT" -> GraphStructDefinition()

				else -> throw DefinitionLoadException("Unknown definition type $type")
			}
			def.srcFile = srcFile

			def.parse(xml)

			return def
		}
	}
}

class DefinitionReference(val name: String, val defName: String, val category: String)
{
	var definition: DataDefinition? = null
}