// Generated by delombok at Sat Jul 14 04:26:21 CEST 2018
/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */
package com.skcraft.launcher.model.launcher

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer

@Serializable
data class LaunchModifier(
    @Optional
    val flags: List<String> = emptyList()
) {
    @Serializer(forClass = LaunchModifier::class)
    companion object
}
