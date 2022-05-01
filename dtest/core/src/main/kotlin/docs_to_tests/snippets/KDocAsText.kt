package docs_to_tests.snippets

import org.jetbrains.kotlin.kdoc.psi.api.KDoc

fun KDoc.asText() = text?.split("\n") ?: listOf()


