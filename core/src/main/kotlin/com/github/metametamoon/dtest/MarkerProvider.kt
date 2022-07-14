package com.github.metametamoon.dtest

import com.github.metametamoon.dtest.extraction.snippets.MarkdownSnippetExtractor
import com.github.metametamoon.dtest.extraction.snippets.childrenNoWhitespaces
import com.github.metametamoon.dtest.extraction.snippets.textWithoutAsterisks
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.kdoc.psi.impl.KDocSection

/**
 *
 *
 * ok
 */
class MarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val kdocSection = element.parent
        return if (kdocSection is KDocSection && element.elementType == KDocTokens.LEADING_ASTERISK) {
            val kdoc = kdocSection.parent as? KDoc ?: return null
            val linesWithSnippets = MarkdownSnippetExtractor().getLinesWithTestStarts(kdoc.textWithoutAsterisks())
            val lineOfThis = kdocSection.childrenNoWhitespaces.filter { it.elementType == KDocTokens.LEADING_ASTERISK }
                .indexOf(element) + 1 // 1 - the first KDoc line with a KDOC_START, not asterisk
            val testNumber = linesWithSnippets.indexOf(lineOfThis)
            if (testNumber != -1) {
                LineMarkerInfo(
                    element,
                    element.textRange,
                    AllIcons.Gutter.Unique,
                    { "Test $testNumber" },
                    { _, _ -> },
                    GutterIconRenderer.Alignment.CENTER,
                    { "Me" }
                )
            } else null
        } else null
    }
}