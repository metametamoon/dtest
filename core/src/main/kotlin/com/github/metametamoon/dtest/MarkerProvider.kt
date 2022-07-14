package com.github.metametamoon.dtest

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.kdoc.psi.api.KDoc


class MarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        return if (element is KDoc) {
//            val owner = element.owner ?: return null
//            val fqName = owner.getKotlinFqName()
            null
        } else null
    }
}