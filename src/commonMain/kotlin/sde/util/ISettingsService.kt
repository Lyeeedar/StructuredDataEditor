package sde.util

import pl.treksoft.kvision.annotations.KVService

@KVService
interface ISettingsService {
    suspend fun loadSettings(): Settings
    suspend fun saveSettings(settings: Settings): Boolean
}