package sde

actual class PingService : IPingService
{

    override suspend fun ping(message: String): String {
        println(message)
        return "Hello Ems!"
    }
}
