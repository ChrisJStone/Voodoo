// Generated by delombok at Sat Jul 14 04:26:21 CEST 2018
/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */
package com.skcraft.launcher.model.modpack

import com.skcraft.launcher.builder.FnPatternList
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.EnumSerializer

@Serializable
data class Feature(
    @Optional var name: String = "",
    @Optional var selected: Boolean = false,
    @Optional var description: String = "",
    @Optional var recommendation: Recommendation? = null,
    @Optional
    @Serializable(with = FnPatternList.Companion::class)
    var files: FnPatternList = FnPatternList()
) {
    @Serializer(forClass = Feature::class)
    companion object : KSerializer<Feature> {
        override fun serialize(encoder: Encoder, obj: Feature) {
            val elemOutput = encoder.beginStructure(descriptor)
            if (obj.name.isNotEmpty())
                elemOutput.encodeStringElement(descriptor, 0, obj.name)
            elemOutput.encodeBooleanElement(descriptor, 1, obj.selected)
            if (obj.description != "")
                elemOutput.encodeStringElement(descriptor, 2, obj.description)
            if (obj.recommendation != null) {
                elemOutput.encodeSerializableElement(
                    descriptor,
                    3,
                    EnumSerializer(Recommendation::class),
                    obj.recommendation!!
                )
            }
            if (obj.files != FnPatternList()) {
                elemOutput.encodeSerializableElement(descriptor, 4, FnPatternList.serializer(), obj.files)
            }
            elemOutput.endStructure(descriptor)
        }
    }
}
