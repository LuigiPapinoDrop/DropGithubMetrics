import io.ktor.util.*
import kotlin.time.ExperimentalTime

@KtorExperimentalAPI
@ExperimentalTime
fun main(args :Array<String> ){
    val map = args.map { it.split("=") }.map { it[0] to it[1] }.toMap()
    val githubAccessToken  = map["githubAccessToken"]
    val repositories = map["repositoriesToScan"]

    Github(githubAccessToken, repositories).retrievePrMetrics()
}