package util


private const val kdocLineStarterLength = 3

/**
 * f
 * f
 * f
 */
fun List<String>.trimDocs(): List<String> {
    require(isGoodKDoc()) { "Please be nice about the doc format." }
    return map { it.drop(kdocLineStarterLength) }
}

private fun List<String>.isGoodKDoc(): Boolean {
    if (this.size < 2) return false
    if (this.first() != "/**") return false
    if (this.last().substring(0..2) != " */") return false
    return (this.subList(1, lastIndex - 1).all { it.startsWith(" * ") || it == " *" })
}