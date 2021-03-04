import com.expediagroup.graphql.plugin.gradle.graphql
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.*

val githubAccessToken: String by lazy { readProperty("githubAccessToken") }
val repositories: String by lazy { readProperty("repositoriesToScan") }



plugins {
    application
    kotlin("jvm") version "1.4.21"
    id("com.expediagroup.graphql") version "3.7.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
}

dependencies {
    implementation("com.expediagroup:graphql-kotlin-client:3.7.0")
    implementation("io.ktor:ktor-client-okhttp:1.3.1")
    implementation("io.ktor:ktor-client-logging-jvm:1.3.1")
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.apache.commons:commons-exec:1.3")
}

application {
    mainClass.set("ApplicationKt")
}

graphql {
    client {
        endpoint = "https://api.github.com/graphql"
        packageName = "com.adaptics.drop_github_stats.gql"
        headers["Authorization"] = "bearer $githubAccessToken"

    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
        kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict")

    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "11"
    freeCompilerArgs = listOf("-Xjsr305=strict")
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "11"
}

task("runDefault", JavaExec::class) {
    main = "ApplicationKt"
    classpath = sourceSets["main"].runtimeClasspath
    args = listOf("githubAccessToken=$githubAccessToken", "repositoriesToScan=$repositories")
}

fun readProperty(name: String): String {
    val localFile = File("local.properties")
    if (localFile.exists()) {
        val fis = FileInputStream("local.properties")
        val props = Properties()
        props.load(fis)

        val localProp = props.getProperty(name)
        if (localProp != null) return localProp
    }
    return if (project.hasProperty(name)) {
        project.property(name).toString()
    } else ""
}


