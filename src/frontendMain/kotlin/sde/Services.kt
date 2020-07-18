package sde

import sde.util.DiskService
import sde.util.SettingsService

object Services
{
	val settings = SettingsService()
	val disk = DiskService()
}