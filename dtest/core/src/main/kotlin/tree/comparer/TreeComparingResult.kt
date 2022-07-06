package tree.comparer

import java.io.File

data class PsiPosition(
    val lineNumber: Int,
    val lineOffset: Int
)

data class DifferenceStackElement(
    val comparingMessage: String,
    val expectedFilePsiOffset: PsiPosition,
    val actualFilePsiOffset: PsiPosition
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

    fun toString(expectedFile: File, actualFile: File): String {
        val indent = " "
        val doubleIndent = "  "
        val result = StringBuilder()
        result.appendLine(reason)
        for (differenceStackElement in trace) {
            result.append(indent)
            result.appendLine(differenceStackElement.comparingMessage)
            result.append(doubleIndent)
            result.appendLine("Expected: ${expectedFile.absolutePath}:${stringify(differenceStackElement.expectedFilePsiOffset)}")
            result.append(doubleIndent)
            result.appendLine("Actual: ${actualFile.absolutePath}:${stringify(differenceStackElement.actualFilePsiOffset)}")
        }
        return result.toString()
    }

    private fun stringify(expectedFilePsiOffset: PsiPosition) =
        "${expectedFilePsiOffset.lineNumber + 1}:${expectedFilePsiOffset.lineOffset + 1}"
}