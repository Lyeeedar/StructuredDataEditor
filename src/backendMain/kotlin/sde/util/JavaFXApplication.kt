package sde.util

import javafx.application.Application
import javafx.application.Platform
import javafx.stage.Stage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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

		private suspend fun init()
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