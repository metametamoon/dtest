package util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File

typealias ImportEntry = String
typealias FqFileName = String

@Serializable
data class DtestSettings(
    val imports: Map<FqFileName, List<ImportEntry>> = emptyMap(),
    val defaultTestAnnotationFqName: String = "kotlin.test.Test"
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