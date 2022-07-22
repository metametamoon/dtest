package com.github.metametamoon.dtest

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext

class DtestReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    return if (NavigationUtil.lazyResolveDtest(element) != null)
                        arrayOf(DtestPsiReference(element))
                    else
                        emptyArray()
                }
            })
    }
}

/*
class DtestReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        return if (NavigationUtil.lazyResolveDtest(element) != null)
            arrayOf(DtestPsiReference(element))
        else
            emptyArray()
    }
}*/
