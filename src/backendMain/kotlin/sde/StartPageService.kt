package sde

import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import pl.treksoft.kvision.remote.ServiceException
import sde.project.Project
import sde.project.ProjectUtils
import sde.util.JavaFXApplication
import java.io.File
import java.time.LocalDateTime

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

	override suspend fun openProject(path: String): Project
	{
		val proj = ProjectUtils.readFullProject(path)

		removeRecentProject(path)
		Settings.recentProjects.add(0, RecentProject(path, proj.name, LocalDateTime.now()))

		return proj
	}

	override suspend fun browseExistingProject(): Project
	{
		val chosenFile = JavaFXApplication.execute {
			val browser = FileChooser()
			browser.showOpenDialog(null)
		}

		if (chosenFile == null || chosenFile.name != "ProjectRoot.xml") {
			throw ServiceException("Invalid project root")
		}

		val projectPath = chosenFile.canonicalPath.replace('\\', '/')
		return openProject(projectPath)
	}

	override suspend fun createNewProject(config: NewProjectConfig): Project
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
		return JavaFXApplication.execute {
			val browser = DirectoryChooser()
			browser.showDialog(null).canonicalPath.replace('\\', '/')
		}
	}
}