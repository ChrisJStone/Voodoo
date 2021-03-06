package voodoo.provider

import com.github.kittinunf.fuel.core.extensions.cUrlString
import com.github.kittinunf.fuel.coroutines.awaitObjectResponseResult
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.serialization.kotlinxDeserializerOf
import com.github.kittinunf.result.Result
import kotlinx.coroutines.channels.SendChannel
import voodoo.data.flat.Entry
import voodoo.data.lock.LockEntry
import voodoo.data.provider.UpdateChannel
import voodoo.data.provider.UpdateJson
import voodoo.util.Downloader
import java.io.File

/**
 * Created by nikky on 30/12/17.
 * @author Nikky
 */
object UpdateJsonProvider : ProviderBase("UpdateJson Provider") {
    private suspend fun getUpdateJson(url: String): UpdateJson? {
        val (request, response, result) = url.httpGet()
            .header("User-Agent" to Downloader.useragent)
            .awaitObjectResponseResult(kotlinxDeserializerOf(loader = UpdateJson.serializer()))
        return when (result) {
            is Result.Success -> result.value
            is Result.Failure -> {
                logger.error("getUpdateJson")
                logger.error("url: $url")
                logger.error("cUrl: ${request.cUrlString()}")
                logger.error("response: $response")
                logger.error(result.error.exception) { "cold not get update json" }
                result.error.exception.printStackTrace()
                null
            }
        }
    }

    override suspend fun resolve(
        entry: Entry,
        mcVersion: String,
        addEntry: SendChannel<Pair<Entry, String>>
    ): LockEntry {
        val json = getUpdateJson(entry.updateJson)!!
        if (entry.id.isBlank()) {
            entry.id = entry.updateJson.substringAfterLast('/').substringBeforeLast('.')
        }
        val key = mcVersion + when (entry.updateChannel) {
            UpdateChannel.RECOMMENDED -> "-recommended"
            UpdateChannel.LATEST -> "-latest"
        }
        if (!json.promos.containsKey(key)) {
            logger.error("update-json promos does not contain {}", key)
        }
        val version = json.promos[key]!!
        val url = entry.template.replace("{version}", version)
        return entry.lock {
            this.url = url
            updateJson = entry.updateJson
            jsonVersion = version
        }
    }

    override suspend fun download(entry: LockEntry, targetFolder: File, cacheDir: File): Pair<String?, File> {
        return Providers["DIRECT"].download(entry, targetFolder, cacheDir)
    }

    override suspend fun generateName(entry: LockEntry): String {
        return entry.id
    }

    override suspend fun getProjectPage(entry: LockEntry): String {
        val json = getUpdateJson(entry.updateJson)!!
        return json.homepage
    }

    override suspend fun getVersion(entry: LockEntry): String {
        return entry.jsonVersion
    }

    override fun reportData(entry: LockEntry): MutableList<Pair<Any, Any>> {
        val data = super.reportData(entry)
        data += "update channel" to "`${entry.jsonVersion}`"
        return data
    }
}
