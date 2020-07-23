package sde.data.definition

import sde.data.DataDocument
import sde.data.item.AbstractReferenceItem
import sde.data.item.ReferenceItem
import sde.util.XAttribute
import sde.util.XElement
import sde.utils.DefinitionLoadException

abstract class AbstractReferenceDefinition<D: AbstractReferenceDefinition<D, I>, I: AbstractReferenceItem<D>> : AbstractCompoundDefinition<D, I>()
{
    var nullable = true

    init
    {
        textColour = colours["Primitive"]!!
    }

    protected override fun doParseInstance(node: XElement)
    {
        nullable = node.getAttributeValue("Nullable", nullable)
    }

    protected override fun createItemInstance(document: DataDocument): I
    {
        val item = createItemInstanceInternal(document)

        if (!nullable && contentsMap.size == 1)
        {
            item.create()
        }

        return item
    }
    protected abstract fun createItemInstanceInternal(document: DataDocument): I

    protected override fun loadItemInstance(document: DataDocument, xml: XElement): I
    {
        val item = loadItemInstanceInternal(document)

        val refKey = xml.getAttributeValue("meta:RefKey", "---")
        val def = contentsMap[refKey]

        if (def == null) {
            if (!nullable && contentsMap.size == 1)
            {
                item.create()
            }
        } else {
            item.selectedDefinition = def
            item.setCreatedItem(def.loadItem(document, xml))
        }

        return item
    }
    protected abstract fun loadItemInstanceInternal(document: DataDocument): I

    protected override fun saveItemInstance(item: I): XElement
    {
        val xml = saveItemInstanceInternal(item)

        val citem = item.createdItem
        if (citem != null) {
            val cel = citem.def.saveItem(citem)
            xml.attributes.addAll(cel.attributes)
            xml.children.addAll(cel.children)
            xml.attributes.add(XAttribute("meta:RefKey", citem.def.name))
        }

        return xml
    }
    protected abstract fun saveItemInstanceInternal(item: I): XElement
}

class ReferenceDefinition : AbstractReferenceDefinition<ReferenceDefinition, ReferenceItem>()
{
    override fun createItemInstanceInternal(document: DataDocument): ReferenceItem {
        return ReferenceItem(this, document)
    }

    override fun loadItemInstanceInternal(document: DataDocument): ReferenceItem {
        return ReferenceItem(this, document)
    }

    override fun saveItemInstanceInternal(item: ReferenceItem): XElement {
        return XElement(name)
    }
}