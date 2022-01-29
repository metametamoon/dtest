package extractor

import org.jetbrains.kotlin.name.FqName
import org.junit.jupiter.api.DynamicTest


class Test(
    private val fqName: FqName,
    private val execute: () -> Unit
) {
    fun toDynamicTest(): DynamicTest =
        DynamicTest.dynamicTest(fqName.asString()) { execute() }
}

class SourceLocation(
    private val fileName: String,
    private val line: Int?
) {
    override fun toString(): String =
        "$fileName:${line?.toString() ?: "unknown"}"
}
