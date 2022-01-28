package extractor


class Test(
    val execute: () -> Unit
)

class SourceLocation(
    private val fileName: String,
    private val line: Int?
) {
    override fun toString(): String =
        "$fileName:${line?.toString() ?: "unknown"}"
}
