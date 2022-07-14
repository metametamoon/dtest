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
val PsiElement.childrenNoWhitespaces: List<PsiElement>
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

fun KDoc.textWithoutAsterisks(): List<String> {
    val section = children.firstIsInstanceOrNull<KDocSection>() ?: return emptyList()
    val sectionChildren = section.childrenNoWhitespaces
    val leadingAsteriskElements = listOf(-1) + sectionChildren.mapIndexedNotNull { index, element ->
        if (element.elementType == KDocTokens.LEADING_ASTERISK)
            index
        else
            null
    } + listOf(sectionChildren.size)
    val text = mutableListOf<String>()
    for ((prev, cur) in leadingAsteriskElements.windowed(2)) {
        when (cur - prev) {
            1 -> text.add("")
            0 -> throw IllegalArgumentException("Unexpected difference between neighboring leading asterisks: ${cur - prev}")
            else -> (prev + 1 until cur).joinToString(separator = "") { sectionChildren[it].text }
                .let { line -> text.add(line) }
        }
    }
    return text
}


