package voodoo.util.jenkins

import com.github.kittinunf.fuel.core.extensions.cUrlString
import com.github.kittinunf.fuel.coroutines.awaitByteArrayResponseResult
import com.github.kittinunf.fuel.coroutines.awaitObjectResponseResult
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.serialization.kotlinxDeserializerOf
import com.github.kittinunf.result.Result
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mu.KLogging
import voodoo.util.UtilConstants
import java.io.File

object Jenkins : KLogging()

private val json = Json(strictMode = false, encodeDefaults = false)
private val useragent = "voodoo/${UtilConstants.VERSION}"

suspend fun downloadVoodoo(
    component: String,
    bootstrap: Boolean = true,
    serverUrl: String = "https://ci.elytradev.com",
    job: String = "elytra/Voodoo/master",
    binariesDir: File
): File {
    val moduleName = "${if (bootstrap) "bootstrap-" else ""}$component"
    val fileRegex = "$moduleName-[^-]*(?!-fat)\\.jar"

    val server = JenkinsServer(serverUrl)
    val jenkinsJob = server.getJob(job, useragent)!!
    val build = jenkinsJob.lastSuccessfulBuild?.details(useragent)!!
    val buildNumber = build.number
    Jenkins.logger.info("lastSuccessfulBuild: $buildNumber")
    Jenkins.logger.debug("looking for $fileRegex")
    val re = Regex(fileRegex)
    val artifact = build.artifacts.find {
        Jenkins.logger.debug(it.fileName)
        re.matches(it.fileName)
    }
    if (artifact == null) {
        Jenkins.logger.error("did not find {} in {}", fileRegex, build.artifacts)
        throw Exception()
    }
    val artifactUrl = build.url + "artifact/" + artifact.relativePath
    val tmpFile = File(binariesDir, "$moduleName-$buildNumber.tmp")
    val targetFile = File(binariesDir, "$moduleName-$buildNumber.jar")
    val (request, response, result) = artifactUrl.httpGet()
        .header("User-Agent" to useragent)
        .awaitByteArrayResponseResult()
    val content = when (result) {
        is Result.Success -> result.value
        is Result.Failure -> {
            Jenkins.logger.error("artifactUrl: $artifactUrl")
            Jenkins.logger.error("cUrl: ${request.cUrlString()}")
            Jenkins.logger.error("response: $response")
            Jenkins.logger.error(result.error.exception) { "unable to download jarfile from $artifactUrl" }
            throw result.error.exception
        }
    }

    tmpFile.writeBytes(content)
    tmpFile.renameTo(targetFile)
    return targetFile
}

class JenkinsServer(
    val serverUrl: String
) {
    fun getUrl(job: String) = serverUrl + "/job/" + job.replace("/", "/job/")

    suspend fun getJob(job: String, useragent: String): Job? {
        val requestURL = getUrl(job) + "/api/json"
        val (request, response, result) = requestURL.httpGet()
            .header("User-Agent" to useragent)
            .awaitObjectResponseResult<Job>(kotlinxDeserializerOf(loader = Job.serializer(), json = json))
        return when (result) {
            is Result.Success -> result.value
            is Result.Failure -> {
                Jenkins.logger.error("requestURL: $requestURL")
                Jenkins.logger.error("cUrl: ${request.cUrlString()}")
                Jenkins.logger.error("response: $response")
                Jenkins.logger.error(result.error.exception) { "unable to get job from $requestURL" }
                null
            }
        }
    }
}

@Serializable
data class Build(
    val number: Int,
    val url: String
) {
    suspend fun details(useragent: String): BuildWithDetails? {
        val buildUrl = "$url/api/json"
        val deserializationStrategy: DeserializationStrategy<BuildWithDetails> = BuildWithDetails.serializer()
        val (request, response, result) = buildUrl.httpGet()
            .header("User-Agent" to useragent)
            .awaitObjectResponseResult(kotlinxDeserializerOf(loader = deserializationStrategy, json = json))
        return when (result) {
            is Result.Success -> result.value
            is Result.Failure -> {
                Jenkins.logger.error("buildUrl: $buildUrl")
                Jenkins.logger.error("cUrl: ${request.cUrlString()}")
                Jenkins.logger.error("response: $response")
                Jenkins.logger.error(result.error.exception) { "unable to get build from $buildUrl" }
                null
            }
        }
    }
}

@Serializable
data class BuildWithDetails(
    val number: Int,
    val url: String,
    val artifacts: List<Artifact>,
    val timestamp: Long
)

@Serializable
data class Job(
    val url: String,
    val name: String,
    val fullName: String,
    val displayName: String,
    val fullDisplayName: String,
    @Optional val builds: List<Build>? = null,
    @Optional val lastSuccessfulBuild: Build? = null,
    @Optional val lastStableBuild: Build? = null
) {
    suspend fun getBuildByNumber(build: Int, userAgent: String): BuildWithDetails? {
        return builds?.find { it.number == build }?.details(userAgent)
    }
}

@Serializable
data class Artifact(
    val displayPath: String,
    val fileName: String,
    val relativePath: String
)