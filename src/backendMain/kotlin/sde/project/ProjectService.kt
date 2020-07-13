package sde.project

actual class ProjectService : IProjectService
{
	override suspend fun getProject(): Project
	{
		return Project()
	}

}