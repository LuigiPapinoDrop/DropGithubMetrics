package data

import kotlin.time.ExperimentalTime

@ExperimentalTime
data class RepositoryStatsOverall(
    val statsPerDataRange: List<RepositoryStats>,
    val statsOverall: RepositoryStats,
    val repoName: String
)