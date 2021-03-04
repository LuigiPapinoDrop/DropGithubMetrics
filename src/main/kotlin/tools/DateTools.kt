package tools

import java.time.*
import kotlin.time.ExperimentalTime
import kotlin.time.toKotlinDuration

object DateTools {

    /**
     * duration between two instants, excluding weekends
     */
    @ExperimentalTime

    fun betweenInBusinessDays(start: Instant, end: Instant): Double {
        val startLocalDate = start.atZone(ZoneOffset.UTC).toLocalDateTime()
        val endLocalDate = end.atZone(ZoneOffset.UTC).toLocalDateTime()
        return betweenInBusinessDays(startLocalDate, endLocalDate)
    }

    /**
     * duration between two instants, excluding weekends
     */
    @ExperimentalTime
    fun betweenInBusinessDays(start: LocalDateTime, end: LocalDateTime?): Double {
        if (end == null) return 0.0

        val weekendDays = start.toLocalDate().datesUntil(end.toLocalDate())
            .filter { it.dayOfWeek == DayOfWeek.SATURDAY || it.dayOfWeek == DayOfWeek.SUNDAY }
            .count()

        val totalDays = Duration.between(start, end).toKotlinDuration().inDays

        return totalDays - weekendDays
    }


    @ExperimentalTime
    fun betweenInDays(start: Instant, end: Instant): Double {
        return Duration.between(start, end).toKotlinDuration().inDays
    }
}