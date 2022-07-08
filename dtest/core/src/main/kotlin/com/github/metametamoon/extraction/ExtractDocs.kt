package com.github.metametamoon.extraction

import com.github.michaelbull.result.Ok
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtTreeVisitor

fun extractDocs(currentFile: KtFile): Ok<ExtractedDocs> {
    val documentations = mutableListOf<Pair<KtElement, KDoc>>()
    currentFile.accept(object : KtTreeVisitor<Unit>() {
        override fun visitDeclaration(dcl: KtDeclaration, data: Unit?): Void? {
            dcl.docComment?.also { kDoc ->
                documentations.add(dcl to kDoc)
            }
            return super.visitDeclaration(dcl, data)
        }

    })
    return Ok(ExtractedDocs(documentations))
}

