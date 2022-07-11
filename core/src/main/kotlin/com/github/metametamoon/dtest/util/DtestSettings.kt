package com.github.metametamoon.dtest.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File

typealias ImportEntry = String
typealias FqFileName = String
typealias FqPackageOrClassName = String
typealias FqParentName = String

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class DtestSettings(
    val imports: Map<FqFileName, List<ImportEntry>> = emptyMap(),
    val defaultTestAnnotationFqName: String = "kotlin.test.Test",
    val classParents: Map<FqPackageOrClassName, FqParentName> = mapOf()
) {
    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        fun readFromFile(settingsFile: File?): DtestSettings? {
            return when {
                settingsFile == null -> null
                !settingsFile.exists() || !settingsFile.isFile -> null
                else -> Json.decodeFromStream<DtestSettings>(settingsFile.inputStream())
            }
        }
    }
}