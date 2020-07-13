package sde

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.Compression
import io.ktor.routing.routing
import pl.treksoft.kvision.remote.applyRoutes
import pl.treksoft.kvision.remote.kvisionInit
import sde.project.ProjectServiceManager

fun Application.main() {
    install(Compression)
    kvisionInit()

    routing {
        applyRoutes(PingServiceManager)
	    applyRoutes(ProjectServiceManager)
    }
}
