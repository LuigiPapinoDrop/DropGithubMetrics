package data

import format
import tools.DateTools.betweenInBusinessDays
import java.time.LocalDateTime
import kotlin.time.ExperimentalTime

@ExperimentalTime
data class PrStats(
    val name: String,
    val additions: Int,
    val deletions: Int,
    val publishDate: LocalDateTime,
    val mergeDate: LocalDateTime,
    val firstReviewDate: LocalDateTime?,
    val reviewsCount: Int
){

    val mergeInBusinessDays: Double = betweenInBusinessDays(publishDate, mergeDate)
    val firstReviewInBusinessDays: Double = betweenInBusinessDays(publishDate, firstReviewDate)

    fun csv(): String {
        val sb = StringBuilder()

        sb.append("$name,")
        sb.append("$additions,")
        sb.append("$deletions,")
        sb.append("$reviewsCount,")
        sb.append("${mergeInBusinessDays.format(2)},")
        sb.append("${firstReviewInBusinessDays.format(2)},")
        sb.append("$publishDate,")
        sb.append("$mergeDate,")
        sb.append("$firstReviewDate")
        sb.appendLine()
        return sb.toString()
    }

    companion object {
        fun csvHeader(): String {

            return "Name,Additions,Deletions,Reviews Count,Merge Time,First Review Time,Publish Date,Merge Date,First Review Date\n"
        }


    }
}