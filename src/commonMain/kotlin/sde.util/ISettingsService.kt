package sde.util

import io.kvision.annotations.KVService

@KVService
interface ISettingsService {
    suspend fun loadSettings(): Settings
    suspend fun saveSettings(settings: Settings): Boolean
}