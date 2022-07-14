package com.github.metametamoon.dtest.extraction.snippets

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.elementType
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.kdoc.psi.impl.KDocSection
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull

/**
 * Traverses the children of this element without ignoring leaf elements
 */
private val PsiElement.childrenNoWhitespaces: List<PsiElement>
    get() {
        var currentChild: PsiElement? = firstChild
        val allChildren = mutableListOf<PsiElement>()
        while (true) {
            currentChild?.let { currentChildNonNull ->
                allChildren.add(currentChildNonNull)
                currentChild = currentChildNonNull.nextSibling
            } ?: break
        }
        return allChildren.filter { it !is PsiWhiteSpace }
    }


fun KDoc.asText(): List<String> {
    val section = children.firstIsInstanceOrNull<KDocSection>() ?: return emptyList()
    val text = section.childrenNoWhitespaces.filter {
        it.elementType == KDocTokens.TEXT || it.elementType == KDocTokens.CODE_BLOCK_TEXT
        // I don't know the exact case where it is CODE_BLOCK_TEXT, but sometimes it is
    }.map { it.text }
    // we want to assert that the first line of this function's return list corresponds to the line
    // where KDoc starts. So we need to pad it with an empty String if there was no
    // element corresponding to the line
    val firstLine =
        if (section.childrenNoWhitespaces.firstOrNull().elementType == KDocTokens.LEADING_ASTERISK)
            listOf("")
        else emptyList()
    return firstLine + text
}


