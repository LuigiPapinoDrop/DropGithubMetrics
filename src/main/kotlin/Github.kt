import com.adaptics.drop_github_stats.gql.SearchQuery
import com.expediagroup.graphql.client.GraphQLClient
import data.PrStats
import data.RepositoryStats
import data.RepositoryStatsOverall
import io.ktor.client.engine.okhttp.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import tools.GqlClientBuilder
import java.io.File
import java.io.FileOutputStream
import java.time.*
import java.util.Comparator.naturalOrder
import kotlin.time.ExperimentalTime

@ExperimentalTime
class Github(private val githubAccessToken: String?, repositories: String?) {

    val repositoriesList = repositories?.split(",") ?: emptyList()

    /**
     * Fetch the data from GitHub, and save the metrics in the csv folder
     * Look at the config folder for tweaking the parameters
     */
    @KtorExperimentalAPI
    fun retrievePrMetrics() {
        val client = GqlClientBuilder.build(githubAccessToken)

        val now = LocalDate.now()
        val weeks = mutableListOf(now)
        for (i in 1..11) {
            weeks.add(now.minusWeeks(i.toLong()))
        }

        val reposStatsMap = mutableMapOf<String, RepositoryStatsOverall>()

        runBlocking {
            println("start retrieving...")
            repositoriesList.forEach { repoName ->
                val repositoryStatsList = weeks.map { weekDate ->
                    retrievePullRequestsData(
                        client,
                        repoName,
                        startDate = weekDate.minusDays(7),
                        endData = weekDate
                    )
                }
                    .filter { it.pullRequestsCount > 0 }

                val overallStats = calculateOverallStats(repositoryStatsList)

                reposStatsMap[repoName] = RepositoryStatsOverall(repositoryStatsList, overallStats, repoName)

            }
            println("done retrieving")

            saveCSV(reposStatsMap)
        }
    }

    private fun plotStats(map: Map<String, RepositoryStatsOverall>) {
        /*     val mergeTimeData = repositoryStatsList.map { it.mergeTimeInDays }
             gnuplot() {
                 val hereDoc = heredoc(mergeTimeData)
                 invoke("""
                            set term png
                            set output "data/img/$repoName.png"
                            plot $hereDoc  title 'Merge Time' with lines
                         """.trimIndent())
             }*/

    }


    @KtorExperimentalAPI
    @OptIn(ExperimentalTime::class)
    private suspend fun retrievePullRequestsData(
        client: GraphQLClient<OkHttpConfig>,
        repoName: String,
        endData: LocalDate,
        startDate: LocalDate,
    ): RepositoryStats {

        // init stats
        var repositoryStats = RepositoryStats(
            name = repoName,
            dateStart = startDate,
            dateEnd = endData
        )

        // fetch github data
        val dataRange = repositoryStats.getDateRange()
        val searchQuery = "repo:dropkitchen/$repoName is:pr is:merged merged:$dataRange"
        val query = SearchQuery(client)
        val data = query.execute(SearchQuery.Variables(query = searchQuery)).data

        if (data?.search?.nodes.isNullOrEmpty()) {
            return repositoryStats
        }

        // calculate stats
        val pullRequests = data?.search?.nodes as List<SearchQuery.PullRequest>
        val prStatsList = pullRequests.map {
            PrStats(
                name = it.title,
                additions = it.additions,
                deletions = it.deletions,
                publishDate = it.publishedAt.toLocalDateTime(),
                mergeDate = it.mergedAt.toLocalDateTime(),
                firstReviewDate = it.reviews?.nodes?.firstOrNull()?.createdAt?.toLocalDateTime(),
                reviewsCount = it.reviews?.nodes?.size ?: 0
            )
        }
        repositoryStats = repositoryStats.copy(
            name = repoName,
            prsStats = prStatsList
        )
        println(repositoryStats.pretty())
        return repositoryStats
    }


    private fun calculateOverallStats(statsList: List<RepositoryStats>): RepositoryStats {
        val dateStart = statsList.map { it.dateStart }.minOfWith(naturalOrder(), { it })
        val dateEnd = statsList.map { it.dateEnd }.maxOfWith(naturalOrder(), { it })
        val name = statsList.first().name
        val overall = RepositoryStats(
            name = name,
            dateStart = dateStart,
            dateEnd = dateEnd,
            prsStats = statsList.flatMap { it.prsStats }
        )

        println(overall.pretty())
        return overall
    }

    private fun saveCSV(map: Map<String, RepositoryStatsOverall>) {
        File("data").deleteRecursively()
        val allFile = File("data/csv/_all.csv")
        allFile.parentFile.mkdirs()

        val overallCsvFile = FileOutputStream("data/csv/_all.csv")
        val overallWriter = overallCsvFile.writer()
        overallWriter.append(RepositoryStats.csvHeader(true))

        map.values.forEach { stats ->
            val repoName = stats.repoName
            val repositoryStatsList = stats.statsPerDataRange
            val overallStats = stats.statsOverall

            val repoCsvFile = File("data/csv/$repoName/$repoName.csv")
            repoCsvFile.parentFile.mkdirs()
            val repoCsvStream = FileOutputStream(repoCsvFile)
            val repoWriter = repoCsvFile.writer()
            repoWriter.append(RepositoryStats.csvHeader(false))

            repositoryStatsList.forEach {
                repoWriter.append(it.csv(false))
                savePrsStatsCsv(it)
            }

            overallWriter.append(overallStats.csv(true))

            repoWriter.flush()
            repoWriter.close()
            repoCsvStream.close()

            overallWriter.flush()
        }

        overallWriter.close()
        overallCsvFile.close()
        println("csv files saved")
    }

    private fun savePrsStatsCsv(repoStats: RepositoryStats) {
        val repoName = repoStats.name
        val fileName = "${repoStats.name}_${repoStats.dateStart}.csv"

        val repoCsvFile = FileOutputStream("data/csv/$repoName/$fileName")
        val repoWriter = repoCsvFile.writer()
        repoWriter.append(PrStats.csvHeader())

        repoStats.prsStats.forEach {
            repoWriter.append(it.csv())
        }

        repoWriter.flush()
        repoWriter.close()
        repoCsvFile.close()
    }


}

