package sde.util

import kotlinx.serialization.json.Json
import java.io.File

actual class SettingsService : ISettingsService {
    private var settings: Settings? = null
        get() {
            if (field == null) {
                field = try {
                    val json = File(settingsFile).readText()
                    Json.parse(Settings.serializer(), json)
                } catch(ex: Exception) {
                    Settings()
                }
            }

            return field
        }

    private val settingsFile = "sdeSettings.data"

    override suspend fun loadSettings(): Settings {
        return settings!!
    }

    override suspend fun saveSettings(settings: Settings): Boolean {
        val json = Json.stringify(Settings.serializer(), settings)
        File(settingsFile).writeText(json)

        this.settings = settings

        return true
    }
}