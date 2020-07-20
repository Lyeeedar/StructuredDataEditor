package sde.utils

fun String.getFileName(): String = this.split('/', '\\').last()