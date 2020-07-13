package sde

import sde.project.IProjectService
import sde.project.Project

actual class PingService : IPingService
{

    override suspend fun ping(message: String): String {
        println(message)
        return "Hello Ems!"
    }
}