package com.github.metametamoon.extraction.snippets

import org.jetbrains.kotlin.kdoc.psi.api.KDoc

fun KDoc.asText() = text?.split("\n") ?: listOf()


