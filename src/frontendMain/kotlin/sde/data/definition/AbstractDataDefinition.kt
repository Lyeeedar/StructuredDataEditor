package sde.data.definition

import org.w3c.dom.Node
import org.w3c.dom.XMLDocument
import sde.utils.*

typealias DefinitionMap = HashMap<String, AbstractDataDefinition>

abstract class AbstractDataDefinition
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

	val attributes = ArrayList<AbstractPrimitiveDataDefinition>()
	var referenceMap = HashMap<String, DefinitionReference>()

	abstract fun children(): List<AbstractDataDefinition>

	fun registerReference(name: String, defName: String)
	{
		referenceMap[name] = DefinitionReference(name, defName)
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

	fun parse(node: Node)
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
				val def = load(att)

				if (def !is AbstractPrimitiveDataDefinition)
				{
					throw DefinitionLoadException("Cannot put a non-primitive into attributes!")
				}

				attributes.add(def)
			}
		}

		doParse(node)
	}

	abstract fun doParse(node: Node)

	companion object
	{
		fun load(contents: String): AbstractDataDefinition
		{
			val xml = contents.parseXml()

			return load(xml.childNodes.item(0)!!)
		}

		fun load(xml: Node): AbstractDataDefinition
		{
			var type = xml.getAttributeValue("meta:RefKey", "").toUpperCase()
			if (type.endsWith("DEF")) {
				type = type.substring(0, type.length - "DEF".length)
			}

			val def = when(type)
			{
				"NUMBER" -> NumberDefinition()
				else -> throw Exception("Unknown definition type $type")
			}

			def.parse(xml)

			return def
		}
	}
}

class DefinitionReference(val name: String, val defName: String)
{
	var definition: AbstractDataDefinition? = null
}