package data

import format
import median
import percentile
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.time.ExperimentalTime

@ExperimentalTime
data class RepositoryStats(
    val name: String,
    val dateStart: LocalDate,
    val dateEnd: LocalDate,
    val prsStats: List<PrStats> = emptyList()
) {

    val mergeTimeInDays: Double
        get() {
            return prsStats.map { it.mergeInBusinessDays }.average()
        }
    val mergeMedian: Double
        get() {
            return prsStats.map { it.mergeInBusinessDays }.median()
        }

    val mergePercentile3: Double
        get() {
            return prsStats.map { it.mergeInBusinessDays }.percentile(3.0)
        }
    val mergePercentile1: Double
        get() {
            return prsStats.map { it.mergeInBusinessDays }.percentile(1.0)
        }

    val firstReviewTimeInDays: Double
        get() {
            return prsStats.map { it.firstReviewInBusinessDays }.average()
        }

    val pullRequestsCount: Int
        get() {
            return prsStats.count()
        }
    val additionsAverage: Int
        get() {
            return prsStats.map { it.additions }.average().toInt()
        }
    val deletionsAverage: Int
        get() {
            return prsStats.map { it.deletions }.average().toInt()
        }
    val reviewsCountsAverage: Double
        get() {
            return prsStats.map { it.reviewsCount }.average()
        }


    fun pretty(): String {
        val sb = StringBuilder()
        sb.append("Repository: $name,".padEnd(35))
        sb.append("\t$dateStart..$dateEnd\t")
        sb.append("Count: $pullRequestsCount,\t".padEnd(10))
        sb.append("Additions: $additionsAverage,".padEnd(18))
        sb.append("Deletions: $deletionsAverage,".padEnd(18))
        sb.append("Merge Time: ${mergeTimeInDays.format(2)} days,\t")
        sb.append("First Review Time: ${firstReviewTimeInDays.format(2)} days,\t")
        sb.append("Reviews Count Average: ${reviewsCountsAverage.format(2)},\t".padEnd(10))

        return sb.toString()
    }

    fun csv(withRepoName: Boolean): String {
        val sb = StringBuilder()
        if (withRepoName) {
            sb.append("$name,")
        }
        sb.append("$pullRequestsCount,")
        sb.append("$additionsAverage,")
        sb.append("$deletionsAverage,")
        sb.append("${mergeTimeInDays.format(2)},")
        sb.append("${firstReviewTimeInDays.format(2)},")
        sb.append("${reviewsCountsAverage.format(2)},")
        sb.append("${mergeMedian.format(2)},")
        sb.append("${mergePercentile3.format(2)},")
        sb.append("${mergePercentile1.format(2)},")

        sb.append("$dateStart,")
        sb.append("$dateEnd")
        sb.appendLine()
        return sb.toString()
    }


    fun getDateRange(): String {
        val start = dateStart.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val end = dateEnd.format(DateTimeFormatter.ISO_LOCAL_DATE)
        return "$start..$end"
    }

    companion object {
        fun csvHeader(withRepoName: Boolean): String {
            var header =
                "PRs Count,Additions Avg,Deletions Avg,Merge TimeInDays,First Review TimeInDays,Reviews Count Avg,Merge Median,Merge Percentile 3 days,Merge Percentile 1 day,Date Start,Date End\n"
            if (withRepoName) {
                header = "RepoName,$header"
            }
            return header
        }


    }
}