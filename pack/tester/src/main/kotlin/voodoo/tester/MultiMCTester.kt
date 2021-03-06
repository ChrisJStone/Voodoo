package voodoo.tester

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.internal.BooleanSerializer
import kotlinx.serialization.internal.HashMapSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import voodoo.data.Side
import voodoo.data.lock.LockPack
import voodoo.forge.ForgeUtil
import voodoo.mmc.MMCUtil
import voodoo.provider.Providers
import voodoo.util.Downloader
import voodoo.util.blankOr
import voodoo.util.pool
import java.io.File
import java.io.IOException

/**
 * Created by nikky on 06/05/18.
 * @author Nikky
 */

object MultiMCTester : AbstractTester() {
    override val label = "MultiMC Tester"

    override suspend fun execute(modpack: LockPack, clean: Boolean) {
        val folder = "voodoo_test_${modpack.id}"
        val title = "${modpack.title.blankOr ?: modpack.id} Test Instance"

        val cacheDir = directories.cacheHome
        val mmcConfigDir = File("multimc")
        val multimcDir = MMCUtil.findDir()
        val instanceDir = multimcDir.resolve("instances").resolve(folder)

        if (clean) {
            logger.info("cleaning old instance dir ($instanceDir)")
            instanceDir.deleteRecursively()
        }

        instanceDir.mkdirs()

        val minecraftDir = MMCUtil.installEmptyPack(
            title,
            folder,
            icon = modpack.icon,
            mcVersion = modpack.mcVersion,
            forgeVersion = ForgeUtil.forgeVersionOf(modpack.forge)?.forgeVersion
        )

        val modsDir = minecraftDir.resolve("mods")
        modsDir.deleteRecursively()

        val minecraftSrcDir = modpack.sourceFolder
        Downloader.logger.info("copying files into minecraft dir ('$minecraftSrcDir' => '$minecraftDir')")
        if (minecraftSrcDir.exists()) {
            minecraftSrcDir.copyRecursively(minecraftDir, overwrite = true)
        }

        for (src in minecraftSrcDir.walkTopDown()) {
            if (src.name.endsWith(".lock.hjson")) continue
            if (src.name.endsWith(".entry.hjson")) continue

            val relPath = src.relativeTo(minecraftSrcDir)
            val dstFile = minecraftDir.resolve(relPath)

            if (dstFile.exists() && !(src.isDirectory && dstFile.isDirectory)) {
                val stillExists = when {
                    dstFile.isDirectory -> !dstFile.deleteRecursively()
                    else -> !dstFile.delete()
                }

                if (stillExists) {
                    throw FileAlreadyExistsException(
                        file = src,
                        other = dstFile,
                        reason = "The destination file already exists."
                    )
                }
            }

            if (src.isDirectory) {
                dstFile.mkdirs()
            } else {
                logger.debug("copying $src -> $dstFile")
                if (src.copyTo(dstFile, overwrite = true).length() != src.length()) {
                    throw IOException("Source file wasn't copied completely, length of destination file differs.")
                }
            }
        }

        logger.info("sorting client / server mods")
        for (file in minecraftDir.walkTopDown()) {
            when {
//                file.name.endsWith(".lock.hjson") -> file.delete()
//                file.name.endsWith(".entry.hjson") -> file.delete()
                file.name == "_CLIENT" -> {
                    file.copyRecursively(file.parentFile, overwrite = true)
                    file.deleteRecursively()
                }
                file.name == "_SERVER" -> {
                    file.deleteRecursively()
                }
            }
        }

        val json = Json(indented = true, encodeDefaults = false)
        val featureSerializer = HashMapSerializer(String.serializer(), BooleanSerializer)

        // read user input
        val featureJson = instanceDir.resolve("voodoo.features.json")
        val previousSelection = if (featureJson.exists()) {
            json.parse(featureSerializer, featureJson.readText())
        } else {
            mapOf<String, Boolean>()
        }
        val (features, reinstall) = MMCUtil.selectFeatures(
            modpack.features.map { it.feature }, previousSelection,
            modpack.title.blankOr
                ?: modpack.id, modpack.version, forceDisplay = false, updating = featureJson.exists()
        )
        logger.debug("result: features: $features")
        if (!features.isEmpty()) {
            featureJson.createNewFile()
            featureJson.writeText(json.stringify(featureSerializer, features))
        }
        if (reinstall) {
            minecraftDir.deleteRecursively()
        }

        coroutineScope {
            modpack.entrySet.forEach { entry ->
                if (entry.side == Side.SERVER) return@forEach
                launch(pool + CoroutineName(entry.id)) {
                    val folder = minecraftDir.resolve(entry.serialFile).absoluteFile.parentFile

                    val matchedFeatureList = modpack.features.filter { it.entries.contains(entry.id) }
                    if (!matchedFeatureList.isEmpty()) {
                        val download = matchedFeatureList.any { features[it.feature.name] ?: false }
                        if (!download) {
                            MMCUtil.logger.info("${matchedFeatureList.map { it.feature.name }} is disabled, skipping download")
                            return@launch
                        }
                    }
                    val provider = Providers[entry.provider]
                    val targetFolder = minecraftDir.resolve(folder)
                    val (url, file) = provider.download(entry, targetFolder, cacheDir)
                }
            }
        }

        logger.info("clearing serverside files")
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

//        MMCUtil.startInstance(title)
        MMCUtil.startInstance(folder)
    }
}