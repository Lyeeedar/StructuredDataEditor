package sde

import javafx.application.Application
import javafx.stage.FileChooser
import javafx.stage.Stage
import pl.treksoft.kvision.remote.ServiceException
import sde.project.Project
import java.io.File
import java.rmi.ServerException
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
		TODO("Not yet implemented")
	}

	override suspend fun browseExistingProject(): Project
	{
		Application.launch(FileChooserApplication::class.java)

		val chosenFile = File("")

		if (chosenFile.name != "ProjectRoot.xml") {
			throw ServiceException("Invalid project root")
		}

		val projectPath = chosenFile.canonicalPath
		removeRecentProject(projectPath)
		Settings.recentProjects.add(RecentProject(projectPath, LocalDateTime.now()))

		return Project()
	}

	override suspend fun createNewProject(): Project
	{
		TODO("Not yet implemented")
	}

}

class FileChooserApplication() : Application()
{
	override fun start(primaryStage: Stage?)
	{
		val browser = FileChooser()
		browser.extensionFilters.add(FileChooser.ExtensionFilter("xml", ".xml"))
		val chosenFile = browser.showOpenDialog(null)
	}
}