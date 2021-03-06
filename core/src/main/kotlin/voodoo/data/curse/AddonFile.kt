package voodoo.data.curse

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import voodoo.util.serializer.DateSerializer
import java.util.Date

@Serializable
data class AddonFile(
    val id: FileID,
    val fileName: String,
    val fileNameOnDisk: String,
    @Serializable(with=DateSerializer::class)
    val fileDate: Date,
    var releaseType: FileType,
    val fileStatus: FileStatus,
    val downloadURL: String,
    val alternate: Boolean,
    val alternateFileId: Int,
    val dependencies: List<AddOnFileDependency>?,
    val available: Boolean,
    var modules: List<AddOnModule>?,
    val packageFingerprint: Long,
    val gameVersion: List<String>,
    val installMetadata: String?,
    val fileLength: Long
)