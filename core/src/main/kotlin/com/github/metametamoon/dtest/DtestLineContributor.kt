package com.github.metametamoon.dtest

import com.github.michaelbull.result.mapBoth
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.idea.highlighter.KotlinTestRunLineMarkerContributor

class DtestLineContributor : RunLineMarkerContributor() {
    private val kotlinTestMarkerContributor = KotlinTestRunLineMarkerContributor()
    override fun getInfo(element: PsiElement): Info? {
        return NavigationUtil.resolveDtest(element)?.mapBoth(
            success = { foundDeclaration -> kotlinTestMarkerContributor.getInfo(foundDeclaration) },
            failure = { errorMessage -> Info(AllIcons.Nodes.C_private, { errorMessage }) }
        )
    }
}