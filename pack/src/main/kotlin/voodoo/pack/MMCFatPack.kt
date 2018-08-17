package voodoo.pack

import blue.endless.jankson.Jankson
import voodoo.data.Side
import voodoo.data.lock.LockPack
import voodoo.mmc.MMCUtil
import voodoo.provider.Provider
import voodoo.util.*
import java.io.File

object MMCFatPack : AbstractPack() {
    override val label = "MultiMC Packer (frozen pack)"

    override suspend fun download(modpack: LockPack, target: String?, clean: Boolean, jankson: Jankson) {
        val targetDir = File(target ?: ".multimc")
        val cacheDir = directories.cacheHome
        val instanceDir = cacheDir.resolve("MMC_FAT").resolve(modpack.id)
        val title = modpack.title.blankOr ?: modpack.id
        instanceDir.deleteRecursively()

        instanceDir.mkdirs()

        val iconFile = File("multimc").resolve("${modpack.id}.icon.png")
        val minecraftDir = MMCUtil.installEmptyPack(title, modpack.id, icon = iconFile, instanceDir = instanceDir, mcVersion = modpack.mcVersion, forgeBuild = modpack.forge)

        minecraftDir.mkdirs()
        val modsDir = minecraftDir.resolve("mods")
        modsDir.deleteRecursively()

        downloader.logger.info("copying files into minecraft dir")
        val minecraftSrcDir = modpack.sourceFolder
        if (minecraftSrcDir.exists()) {
            minecraftSrcDir.copyRecursively(minecraftDir, overwrite = true)
        }

        for (file in minecraftDir.walkTopDown()) {
            when {
                file.name == "_CLIENT" -> {
                    file.copyRecursively(file.parentFile, overwrite = true)
                    file.deleteRecursively()
                }
                file.name == "_SERVER" -> {
                    file.deleteRecursively()
                }
            }
        }

        // read user input
        val featureJson = instanceDir.resolve("voodoo.features.json")
        val previousSelection = if (featureJson.exists()) {
            featureJson.readJson()
        } else {
            mapOf<String, Boolean>()
        }
        val (features, reinstall) = MMCUtil.selectFeatures(modpack.features.map { it.properties }, previousSelection,
                modpack.title.blankOr
                        ?: modpack.id, modpack.version, forceDisplay = false, updating = featureJson.exists())
        logger.debug("result: features: $features")
        if (!features.isEmpty()) {
            featureJson.createNewFile()
            featureJson.writeJson(features)
        }
        if (reinstall) {
            minecraftDir.deleteRecursively()
        }

        for ((name, pair) in modpack.entriesMapping) {
            val (entry, entryFile) = pair

            if (entry.side == Side.SERVER) continue
            val folder = minecraftDir.resolve(entryFile).absoluteFile.parentFile

            val matchedFeatureList = modpack.features.filter { it.entries.contains(entry.id) }
            val selected = !matchedFeatureList.isEmpty() && matchedFeatureList.any { features[it.properties.name] ?: false }

            val provider = Provider.valueOf(entry.provider).base
            val targetFolder = minecraftDir.resolve(folder)
            val (url, file) = provider.download(entry, targetFolder, cacheDir)
            if(!selected) {
                file.renameTo(file.parentFile.resolve(file.name + ".disabled"))
            }
        }

        for (file in minecraftDir.walkTopDown()) {
            when {
                file.name == "_CLIENT" -> {
                    file.copyRecursively(file.parentFile, overwrite = true)
                    file.deleteRecursively()
                }
                file.name == "_SERVER" -> {
                    file.deleteRecursively()
                }
            }
        }

        targetDir.mkdirs()
        val instanceZip = targetDir.resolve(modpack.id + ".zip")

        instanceZip.delete()
        packToZip(instanceDir.toPath(), instanceZip.toPath())
        logger.info("created mmc pack $instanceZip")
    }
}