package sde.utils

fun <T> List<T>.tryGet(index: Int): T? {
	if (this.size > index) return this[index]
	return null
}