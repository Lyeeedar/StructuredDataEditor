package sde.util

import kotlinx.serialization.json.Json
import node.fs.fs

actual class SettingsService : ISettingsService {
    private var settings: Settings? = null
        get() {
            if (field == null) {
                field = try {
                    val json = fs.readFileStringSync(settingsFile, "utf8")
                    Json.parse(Settings.serializer(), json)
                } catch(ex: Throwable) {
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
	    this.settings = settings
	    try {
		    val json = Json.stringify(Settings.serializer(), settings)
		    fs.writeFileSync(settingsFile, json)
		    return true
	    } catch (ex: Throwable) {
		    return false
	    }
    }
}