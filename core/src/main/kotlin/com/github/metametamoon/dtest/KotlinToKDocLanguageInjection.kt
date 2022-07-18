package com.github.metametamoon.dtest

import com.github.metametamoon.dtest.extraction.snippets.childrenNoWhitespaces
import com.github.metametamoon.dtest.util.PsiLeafElementInjectionHostAdapter
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.elementType
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens
import org.jetbrains.kotlin.kdoc.psi.api.KDoc

class KotlinToKDocLanguageInjection : MultiHostInjector {
    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        if (context is KDoc) {
            val section = context.getDefaultSection()
            val texts = section.childrenNoWhitespaces.filter { it.elementType == KDocTokens.TEXT }
            if (texts.size >= 2 && texts.first().text.contains("[[") && texts.last().text.contains("]]")) {
                val textsToInject = texts.drop(1).dropLast(1)
                if (textsToInject.isNotEmpty()) {
                    registrar.startInjecting(KotlinLanguage.INSTANCE)
                    textsToInject.filterIsInstance<LeafPsiElement>().forEach { element ->
                        registrar.addPlace(
                            "",
                            "",
                            PsiLeafElementInjectionHostAdapter(element),
                            TextRange(0, element.text.indices.last + 1)
                        )
                    }
                    registrar.doneInjecting()
                }
            }

        }
    }

    override fun elementsToInjectIn(): List<Class<out PsiElement>> = listOf(KDoc::class.java)
}