// Generated by delombok at Sat Jul 14 01:46:55 CEST 2018
/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */
package com.skcraft.launcher.builder

import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.EnumSet
import com.skcraft.launcher.builder.ClientFileCollector.Companion.getDirectoryBehavior
import kotlinx.serialization.json.JSON
import mu.KLogging
import org.apache.commons.io.FilenameUtils.*

class FileInfoScanner : DirectoryWalker() {
    val patterns = ArrayList<FeaturePattern>()

    override fun getBehavior(name: String): DirectoryWalker.DirectoryBehavior {
        return getDirectoryBehavior(name)
    }

    @Throws(IOException::class)
    override fun onFile(file: File, relPath: String) {
        if (file.name.endsWith(FILE_SUFFIX)) {
            val fnPattern = separatorsToUnix(getPath(relPath)) + getBaseName(getBaseName(file.name)) + "*"
            val info: FileInfo = JSON.parse(file.readText()) // mapper.readValue<FileInfo>(file)
            val feature = info.feature
            if (feature != null) {
                if(feature.name.isEmpty()) {
                    throw IllegalStateException("Empty component name found in ${file.absolutePath}")
                }
                val stringPatterns = ArrayList<String>()
                stringPatterns.add(fnPattern)
                val patternList = FnPatternList()
                patternList.include = stringPatterns
                patternList.flags = MATCH_FLAGS
                val fp = FeaturePattern(feature = feature, filePatterns = patternList)
                this.patterns += fp
                logger.info("Found .info.json file at ${file.absolutePath}, with pattern $fnPattern, and component $feature")
            }
        }
    }

    companion object: KLogging() {
        private val MATCH_FLAGS = EnumSet.of<FnMatch.Flag>(FnMatch.Flag.CASEFOLD, FnMatch.Flag.PERIOD, FnMatch.Flag.PATHNAME)
        const val FILE_SUFFIX = ".info.json"
    }
}