// Generated by delombok at Sat Jul 14 01:46:55 CEST 2018
/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */
package com.skcraft.launcher.builder

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import java.io.File

class BuilderOptions(parser: ArgParser) {
    // Configuration
    // Override config
    val name by parser.storing("--name", help = "")
        .default<String?>(null)
    val title: String? by parser.storing("--title", help = "")
        .default<String?>(null)
    val gameVersion: String? by parser.storing("--mc-version", help = "")
        .default<String?>(null)
    // Required
    val version: String by parser.storing("--version", help = "")
    val manifestPath: File by parser.storing("--manifest-dest", help = "") { File(this) }
    // Overall paths
    val inputPath: File? by parser.storing("--input", "-i", help = "") { File(this) }
        .default<File?>(null)
    val outputPath: File? by parser.storing("--output", "-o", help = "") { File(this) }
        .default<File?>(null)
    // Input paths
    val configPath by parser.storing("--config", help = "") { File(this) }
        .default { File(inputPath!!, DEFAULT_CONFIG_FILENAME) }
    val versionManifestPath by parser.storing("--version-file", help = "") { File(this) }
        .default { File(inputPath!!, DEFAULT_VERSION_FILENAME) }
    val filesDir by parser.storing("--files", help = "") { File(this) }
        .default { File(inputPath!!, DEFAULT_SRC_DIRNAME) }
    val loadersDir by parser.storing("--loaders", help = "") { File(this) }
        .default { File(inputPath!!, DEFAULT_LOADERS_DIRNAME) }
    // Output paths

    val objectsDir by parser.storing("--objects-dest", help = "") { File(this) }
        .default { File(outputPath!!, objectsLocation) }
    val librariesDir by parser.storing("--libraries-dest", help = "") { File(this) }
        .default { File(outputPath!!, librariesLocation) }
    val librariesLocation: String by parser.storing("--libs-url", help = "").default("libraries")
    val objectsLocation: String by parser.storing("--objects-url", help = "").default("objects")
    // Misc
    val isPrettyPrinting by parser.flagging("--pretty-print", help = "")
        .default(false)

    companion object {
        const val DEFAULT_CONFIG_FILENAME = "modpack.json"
        const val DEFAULT_VERSION_FILENAME = "version.json"
        const val DEFAULT_SRC_DIRNAME = "src"
        const val DEFAULT_LOADERS_DIRNAME = "loaders"
    }
}
