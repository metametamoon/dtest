package com.github.metametamoon.dtest

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.kdoc.psi.api.KDoc

class MarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        return if (element is KDoc) {
            LineMarkerInfo(
                element,
                element.textRange,
                AllIcons.Actions.Rerun,
                { "Tooltip" },
                { a, b: Any -> },
                GutterIconRenderer.Alignment.CENTER,
                { "LineMarker name" }
            )
        } else null
    }
}