package com.github.metametamoon.dtest

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

class DtestPsiReference(myElement: PsiElement) : PsiReferenceBase<PsiElement>(myElement) {
    override fun resolve(): PsiElement? = NavigationUtil.resolveDtest(element)?.component1()
}