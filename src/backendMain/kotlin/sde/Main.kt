package sde

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.routing.routing
import io.kvision.remote.applyRoutes
import io.kvision.remote.kvisionInit
import sde.util.DiskServiceManager
import sde.util.SettingsServiceManager

fun Application.main() {
    install(Compression)
	install(CallLogging)

    routing {
	    applyRoutes(DiskServiceManager)
        applyRoutes(SettingsServiceManager)
    }

	kvisionInit()
}
