package tree.comparer

import com.intellij.openapi.editor.Document
import java.io.File

data class DifferenceStackElement(
    val comparingMessage: String,
    val expectedFilePsiOffset: Int,
    val actualFilePsiOffset: Int
)


sealed interface TreeComparingResult {
    fun areSame(): Boolean
    fun wrapIn(
        differenceStackElement: DifferenceStackElement
    ): TreeComparingResult
}

object Same : TreeComparingResult {
    override fun areSame(): Boolean = true

    override fun wrapIn(
        differenceStackElement: DifferenceStackElement
    ): Same = this

    override fun toString(): String = "Same"
}

data class Different(val reason: String, val trace: List<DifferenceStackElement> = emptyList()) : TreeComparingResult {
    override fun areSame(): Boolean = false

    override fun wrapIn(differenceStackElement: DifferenceStackElement): TreeComparingResult =
        Different(reason, trace + listOf(differenceStackElement))

    fun toString(
        expectedFile: File,
        expectedDocument: Document,
        actualFile: File,
        actualDocument: Document
    ): String {
        val indent = " "
        val doubleIndent = "  "
        val result = StringBuilder()
        result.appendLine(reason)
        for (differenceStackElement in trace) {
            result.append(indent)
            result.appendLine(differenceStackElement.comparingMessage)
            result.append(doubleIndent)
            result.appendLine(
                "Expected: ${expectedFile.absolutePath}:${
                    stringify(differenceStackElement.expectedFilePsiOffset, expectedDocument)
                }"
            )
            result.append(doubleIndent)
            result.appendLine(
                "Actual: ${actualFile.absolutePath}:${
                    stringify(differenceStackElement.actualFilePsiOffset, actualDocument)
                }"
            )
        }
        return result.toString()
    }

    private fun stringify(expectedFilePsiOffset: Int, expectedDocument: Document): String {
        val lineNumber = expectedDocument.getLineNumber(expectedFilePsiOffset)
        val columnNumber = expectedFilePsiOffset - expectedDocument.getLineStartOffset(lineNumber)
        return "${lineNumber + 1}:${columnNumber + 1}"
    }
}