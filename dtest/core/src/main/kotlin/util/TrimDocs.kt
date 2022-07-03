package util


private const val kdocLineStarterLength = 3

/**
 * f
 * f
 * f
 */
fun List<String>.trimDocs(): List<String> = map { it.drop(kdocLineStarterLength) }

