package sde

import pl.treksoft.kvision.remote.ServiceException
import sde.project.ProjectDef
import sde.project.ProjectUtils
import java.io.File
import java.time.LocalDateTime
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JPanel


actual class StartPageService : IStartPageService
{
	override suspend fun getRecentProjects(): List<RecentProject>
	{
		return Settings.recentProjects
	}

	override suspend fun removeRecentProject(path: String): Boolean
	{
		var didRemove = false
		val itr = Settings.recentProjects.iterator()
		while (itr.hasNext()) {
			val item = itr.next()

			if (item.path == path) {
				itr.remove()
				didRemove = true
			}
		}

		return didRemove
	}

	override suspend fun openProject(path: String): ProjectDef
	{
		val proj = ProjectUtils.readFullProject(path)

		removeRecentProject(path)
		Settings.recentProjects.add(0, RecentProject(path, proj.name, LocalDateTime.now()))

		return proj
	}

	override suspend fun browseExistingProject(): ProjectDef
	{
		val chosenFile = chooseFile()

		if (chosenFile == null || chosenFile.name != "ProjectRoot.xml") {
			throw ServiceException("Invalid project root")
		}

		val projectPath = chosenFile.canonicalPath.replace('\\', '/')
		return openProject(projectPath)
	}

	override suspend fun createNewProject(config: NewProjectConfig): ProjectDef
	{
		val rootFolder = File(config.rootFolder)
		val defsFolder = File(config.defsFolder)

		if (!rootFolder.exists())
		{
			rootFolder.mkdirs()
		}
		if (!defsFolder.exists())
		{
			defsFolder.mkdirs()
		}

		val projRoot = File(rootFolder, "ProjectRoot.xml")

		val contents = """
			<Project>
				<Name>${config.name}</Name>
				<Definitions>${defsFolder.toRelativeString(rootFolder)}</Definitions>
			</Project>
		""".trimIndent()

		projRoot.writeText(contents)

		return openProject(projRoot.canonicalPath)
	}

	override suspend fun browseFolder(): String
	{
		return chooseFile {
			it.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
		}!!.canonicalPath.replace('\\', '/')
	}
}

fun chooseFile(init: ((JFileChooser)->Unit)? = null): File?
{
	val frame = JFrame()
	frame.isVisible = true
	frame.extendedState = JFrame.ICONIFIED
	frame.extendedState = JFrame.NORMAL

	val fc = JFileChooser()

	init?.invoke(fc)

	if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(null)) {
		frame.isVisible = false
		frame.dispose()
		return fc.selectedFile
	}
	frame.isVisible = false
	frame.dispose()
	return null
}