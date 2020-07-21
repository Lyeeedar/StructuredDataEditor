package sde.ui

import com.github.snabbdom.VNode
import org.w3c.dom.events.MouseEvent
import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.form.FormInput
import pl.treksoft.kvision.form.InputSize
import pl.treksoft.kvision.form.ValidationStatus
import pl.treksoft.kvision.form.text.AbstractTextInput
import pl.treksoft.kvision.state.ObservableState

/**
 * The input component rendered as HTML *input type="color"*.
 *
 * @constructor
 * @param value color
 * @param classes a set of CSS class names
 */
class ColorInput(
	value: Color = Color.name(Col.BLACK),
	classes: Set<String> = setOf()
                         ) : Widget(classes), FormInput, ObservableState<Color> {

	protected val observers = mutableListOf<(Color) -> Unit>()

	init {
		this.setInternalEventListener<ColorInput> {
			input = {
				self.changeValue()
			}
		}
	}

	/**
	 * The selection state of the input.
	 */
	var value by refreshOnUpdate(value) { refreshState(); observers.forEach { ob -> ob(it) } }

	/**
	 * The value attribute of the generated HTML input element.
	 *
	 * This value is placed directly in generated HTML code, while the [value] property is dynamically
	 * bound to the input selection state.
	 */
	var startValue by refreshOnUpdate(value) { this.value = it; refresh() }

	/**
	 * The name attribute of the generated HTML input element.
	 */
	override var name: String? by refreshOnUpdate()

	/**
	 * Determines if the field is disabled.
	 */
	override var disabled by refreshOnUpdate(false)

	/**
	 * The size of the input.
	 */
	override var size: InputSize? by refreshOnUpdate()

	/**
	 * The validation status of the input.
	 */
	override var validationStatus: ValidationStatus? by refreshOnUpdate()

	/**
	* @suppress
	* Internal function
	*/
	protected open fun changeValue() {
		val v = getElementJQuery()?.`val`() as String?
		if (v != null && v.isNotEmpty()) {
			this.value = Color(v)
		} else {
			this.value = Color.name(Col.BLACK)
		}
	}

	override fun render(): VNode {
		return render("input")
	}

	override fun getSnClass(): List<StringBoolPair> {
		val cl = super.getSnClass().toMutableList()
		validationStatus?.let {
			cl.add(it.className to true)
		}
		size?.let {
			cl.add(it.className to true)
		}
		return cl
	}

	override fun getSnAttrs(): List<StringPair> {
		val sn = super.getSnAttrs().toMutableList()
		sn.add("type" to "color")
		name?.let {
			sn.add("name" to it)
		}
		if (disabled) {
			sn.add("disabled" to "disabled")
		}
		return sn
	}

	override fun afterInsert(node: VNode) {
		refreshState()
	}

	protected open fun refreshState() {

	}

	/**
	 * A convenient helper for easy setting onClick event handler.
	 */
	open fun onClick(handler: ColorInput.(MouseEvent) -> Unit): ColorInput
	{
		this.setEventListener<ColorInput> {
			click = { e ->
				self.handler(e)
			}
		}
		return this
	}

	/**
	 * Makes the input element focused.
	 */
	override fun focus() {
		getElementJQuery()?.focus()
	}

	/**
	 * Makes the input element blur.
	 */
	override fun blur() {
		getElementJQuery()?.blur()
	}

	override fun getState(): Color = value

	override fun subscribe(observer: (Color) -> Unit): () -> Unit {
		observers += observer
		observer(value)
		return {
			observers -= observer
		}
	}
}
