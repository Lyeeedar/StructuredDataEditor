package sde.util

actual class SettingsService : ISettingsService {
    override suspend fun loadSettings(): Settings {
        TODO("Not yet implemented")
    }

    override suspend fun saveSettings(settings: Settings): Boolean {
        TODO("Not yet implemented")
    }
}