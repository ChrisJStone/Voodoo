package voodoo.provider.impl

import aballano.kotlinmemoization.memoize
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import mu.KLogging
import voodoo.data.flat.Entry
import voodoo.data.flat.ModPack
import voodoo.data.lock.LockEntry
import voodoo.provider.Provider
import voodoo.provider.ProviderBase
import voodoo.provider.UpdateChannel

/**
 * Created by nikky on 30/12/17.
 * @author Nikky
 * @version 1.0
 */
class UpdateJsonProviderThing : ProviderBase {
    override val name = "UpdateJson Provider"

    companion object : KLogging() {
        val mapper = jacksonObjectMapper() // Enable YAML parsing
                .registerModule(KotlinModule()) // Enable Kotlin support

        val getUpdateJson = { url: String ->
            val (request, response, result) = url.httpGet()
                    .responseString()
            when (result) {
                is Result.Success -> {
                    mapper.readValue<UpdateJson>(result.value)
                }
                else -> null
            }
        }.memoize()
    }

    override fun resolve(entry: Entry, modpack: ModPack, addEntry: (Entry) -> Unit): LockEntry {
        val json = getUpdateJson(entry.updateJson)!!
        if (entry.name.isBlank()) {
            entry.name = entry.updateJson.substringAfterLast('/').substringBeforeLast('.')
        }
        val key = modpack.mcVersion + when (entry.updateChannel) {
            UpdateChannel.RECOMMENDED -> "-recommended"
            UpdateChannel.LATEST -> "-latest"
        }
        if (!json.promos.containsKey(key)) {
            logger.error("update-json promos does not contain {}", key)
        }
        val version = json.promos[key]!!
        val url = entry.template.replace("{version}", version)
        return LockEntry(
                provider = Provider.DIRECT.toString(),
                name = entry.name,
                useUrlTxt = entry.useUrlTxt,
                fileName = entry.fileName,
                side = entry.side,
                url = url
        )
    }
}

data class UpdateJson(
        val homepage: String = "",
        val promos: Map<String, String> = emptyMap()
)