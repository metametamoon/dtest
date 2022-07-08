package com.github.metametamoon.extraction

import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.psi.KtElement

class ExtractedDocs(
    val documentations: List<Pair<KtElement, KDoc>>
)
