package sde

import javafx.application.Application
import javafx.application.Platform
import javafx.stage.FileChooser
import javafx.stage.Stage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import pl.treksoft.kvision.remote.ServiceException
import sde.project.Project
import java.io.File
import java.rmi.ServerException
import java.time.LocalDateTime
import java.util.concurrent.Future

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
		removeRecentProject(path)
		Settings.recentProjects.add(RecentProject(path, LocalDateTime.now()))

		return Project()
	}

	override suspend fun browseExistingProject(): Project
	{
		val chosenFile = JavaFXApplication.execute {
			val browser = FileChooser()
			//browser.extensionFilters.add(FileChooser.ExtensionFilter("xml", "xml"))
			browser.showOpenDialog(null)
		}

		if (chosenFile == null || chosenFile.name != "ProjectRoot.xml") {
			throw ServiceException("Invalid project root")
		}

		val projectPath = chosenFile.canonicalPath.replace('\\', '/')
		return openProject(projectPath)
	}

	override suspend fun createNewProject(): Project
	{
		TODO("Not yet implemented")
	}

}

class JavaFXApplication() : Application()
{
	override fun start(primaryStage: Stage)
	{
		primaryStage.isAlwaysOnTop = true
		Platform.setImplicitExit(false)

		initDeferred?.complete(true)
		initDeferred = null
	}

	companion object
	{
		private var initDeferred: CompletableDeferred<Boolean>? = CompletableDeferred()

		suspend fun init()
		{
			if (initDeferred != null) {
				GlobalScope.launch {
					Application.launch(JavaFXApplication::class.java)
				}
			}

			initDeferred?.await()
		}

		suspend fun <T> execute(func: () -> T): T
		{
			init()

			val deferred = CompletableDeferred<T>()

			Platform.runLater {
				val result = func()
				deferred.complete(result)
			}

			return deferred.await()
		}
	}
}