package sde.util

actual class SettingsService : ISettingsService {
    override suspend fun loadSettings(): Settings {
        return Settings()
    }

    override suspend fun saveSettings(settings: Settings): Boolean {
        return true
    }
}