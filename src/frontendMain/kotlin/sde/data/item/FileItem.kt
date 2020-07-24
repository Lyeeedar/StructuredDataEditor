package sde.data.item

import kotlinx.coroutines.launch
import pl.treksoft.kvision.core.Component
import pl.treksoft.kvision.form.text.TextInputType
import pl.treksoft.kvision.form.text.textInput
import pl.treksoft.kvision.html.Button
import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.panel.DockPanel
import pl.treksoft.kvision.panel.Side
import pl.treksoft.kvision.toast.Toast
import sde.Services
import sde.data.DataDocument
import sde.data.definition.FileDefinition
import sde.ui.AnimatedImage
import sde.utils.*

class FileItem(def: FileDefinition, document: DataDocument) : AbstractDataItem<FileDefinition>(def, document)
{
	val previewDiv = Div()
	var existingAnimatedImage: AnimatedImage? = null

	var value: String by obs(def.default, FileItem::value.name)
		.undoable()
		.updatesComponent()
		.get()

	override val description: String
		get() = value.getFileNameWithoutExtension()

	suspend fun getFullPath(): String {
		val documentPath = document.path.getDirectory()
		if (value.isBlank()) {
			return pathCombine(documentPath, def.basePath)
		}

		var path = if (def.relativeToThis)
			pathCombine(documentPath, def.basePath, value)
		else
			pathCombine(document.project.projectRootFolder, def.basePath, value)

		if (def.stripExtension) {
			for (ext in def.allowedFileTypes) {
				val tpath = "$path.$ext"
				if (exists(tpath)) {
					path = tpath
					break
				}
			}
		} else if (path.getExtension().isBlank()) {
			val dir = path.getDirectory()
			val contents = Services.disk.getFolderContents(dir)

			for (item in contents) {
				if (!item.isDirectory) {
					if (item.path.startsWith(path)) {
						path = item.path
						break
					}
				}
			}
		}

		return path
	}

	override fun isDefaultValue(): Boolean
	{
		return value == def.default
	}

	private suspend fun exists(path: String) = Services.disk.fileExists(path)

	suspend fun loadPreview() {
		existingAnimatedImage?.dispose()
		existingAnimatedImage = null
		previewDiv.removeAll()

		val fullPath = getFullPath()
		if (fullPath.endsWith(".png") && exists(fullPath)) {
			existingAnimatedImage = AnimatedImage(document.scope!!, imagePaths = listOf(fullPath))
			previewDiv.add(existingAnimatedImage!!)
		} else {
			val frames = ArrayList<String>()

			var i = if (exists(fullPath + "_0.png")) 0 else 1
			while (true) {
				val imagePath = fullPath + "_$i.png"
				if (exists(imagePath)) {
					frames.add(imagePath)
				} else {
					break
				}

				i++
			}

			existingAnimatedImage = AnimatedImage(document.scope!!, imagePaths = frames)
			previewDiv.add(existingAnimatedImage!!)
		}
	}

	suspend fun setFile(fullPath: String) {
		if (def.resourceDef != null) {
			val exists = exists(fullPath)
			if (exists) {
				val contents = Services.disk.loadFileString(fullPath)
				val defType = contents.getFileDefType()
				if (defType != def.resourceDef!!.name) {
					Toast.error("The selected files type $defType does not match the expected type ${def.resourceDef!!.name}", "Invalid file")
					return
				}
			}
		}

		var path = fullPath
		if (def.stripExtension)
		{
			val extension = path.getExtension()
			path = path.substring(0, path.length-(extension.length+1))
		}

		val basePath = if (def.relativeToThis)
				pathCombine(document.path.getDirectory(), def.basePath)
			else
				pathCombine(document.project.projectRootFolder, def.basePath)

		val relPath = relPath(path, basePath)

		value = relPath
	}

	suspend fun create() {
		val rdef = def.resourceDef ?: return

		if (value.isBlank()) {
			val basePath = if (def.relativeToThis)
					document.path.getDirectory()
				else
					document.project.projectRootFolder

			val baseName = document.path.getFileNameWithoutExtension() + def.name
			var name = baseName + "." + def.allowedFileTypes.first()

			if (!exists(pathCombine(basePath, name))) {
				if (def.stripExtension) {
					value = baseName
				} else {
					value = name
				}
			} else {
				var index = 2
				while (true) {
					name = baseName + index + "." + def.allowedFileTypes.first()
					if (!exists(pathCombine(basePath, name))) {
						if (def.stripExtension) {
							value = baseName
						} else {
							value = name
						}
						break
					}
					index++
				}
			}
		} else if (exists(getFullPath())) {
			Toast.error("File already exists at ${getFullPath()}", "File already exists")
			return
		}

		document.project.create(rdef, getFullPath())
	}

	override fun getEditorComponent(): Component
	{
		document.scope?.launch {
			loadPreview()
		}
		
		return DockPanel {
			add(Button("Browse").apply {
				onClick {
					document.scope?.launch {
						val file = Services.disk.browseFile()
						setFile(file)
					}
				}
			}, Side.RIGHT)

			if (def.resourceDef != null) {
				val createOpenButtonDiv = Div()

				document.scope?.launch {
					val exists = exists(getFullPath())
					if (exists) {
						createOpenButtonDiv.removeAll()
						createOpenButtonDiv.add(Button("Open").apply {
							onClick {
								document.scope?.launch {
									document.project.open(getFullPath())
								}
							}
						})
					} else {
						createOpenButtonDiv.removeAll()
						createOpenButtonDiv.add(Button("Create").apply {
							onClick {
								document.scope?.launch {
									create()
								}
							}
						})
					}
				}

				add(createOpenButtonDiv, Side.RIGHT)
			}

			add(previewDiv, Side.RIGHT)

			textInput (TextInputType.TEXT, value).apply {
				subscribe {
					this@FileItem.value = it ?: ""
				}
				registerListener(FileItem::value.name) {
					this.value = this@FileItem.value
				}
			}

		}
	}
}