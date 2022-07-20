package com.github.metametamoon.dtest

import com.github.michaelbull.result.mapBoth
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement

class DtestNavigateToTestMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val resolveDtest = NavigationUtil.resolveDtest(element)
        return resolveDtest?.mapBoth(
            success = {
                NavigationGutterIconBuilder.create(AllIcons.General.ArrowRight)
                    .setTarget(it)
                    .setTooltipText("Navigate to dtest")
                    .createLineMarkerInfo(element)
            },
            failure = { null }
        )
    }
}