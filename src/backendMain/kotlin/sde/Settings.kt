package sde

import java.time.LocalDateTime

object Settings
{
	val recentProjects = ArrayList<RecentProject>()

	init
	{
		recentProjects.add(RecentProject("D:/Users/Philip/workspace/MatchDungeon/android/assetsraw/ProjectRoot.xml", "Project", LocalDateTime.now()))
		recentProjects.add(RecentProject("D:/Users/Philip/workspace/PortalClosers/game/assetsraw/ProjectRoot.xml", "Project", LocalDateTime.now()))
	}
}