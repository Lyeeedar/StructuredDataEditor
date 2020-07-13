package sde.project

import pl.treksoft.kvision.annotations.KVService

@KVService
interface IProjectService {
	suspend fun getProject(): Project
}