package tools

import com.expediagroup.graphql.client.GraphQLClient
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import java.net.URL
import java.util.concurrent.TimeUnit

object GqlClientBuilder {


    fun build(githubAccessToken: String?) = GraphQLClient(
        url = URL("https://api.github.com/graphql"),
        engineFactory = OkHttp
    ) {
        engine {
            config {
                connectTimeout(10, TimeUnit.SECONDS)
                readTimeout(60, TimeUnit.SECONDS)
                writeTimeout(60, TimeUnit.SECONDS)
            }
        }
        defaultRequest {
            header("Authorization", "bearer $githubAccessToken")
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.HEADERS
        }
    }
}