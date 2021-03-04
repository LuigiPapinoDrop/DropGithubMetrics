import com.adaptics.drop_github_stats.gql.DateTime
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

fun Double.format(digits: Int) = "%.${digits}f".format(this)

fun DateTime?.toLocalDateTime(): LocalDateTime = if (this== null) LocalDateTime.MIN else Instant.parse(this).atZone(ZoneOffset.UTC).toLocalDateTime()

fun List<Double>.median() = this.sorted().let { (it[it.size / 2] + it[(it.size - 1) / 2]) / 2 }

fun List<Double>.percentile(treshold: Double): Double {
    val matched = this.filter { value -> value < treshold }.size
    val total = this.size
    return matched.toDouble().div(total)
}