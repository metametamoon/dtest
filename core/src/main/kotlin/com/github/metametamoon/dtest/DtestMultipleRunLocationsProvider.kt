package com.github.metametamoon.dtest

import com.github.michaelbull.result.Ok
import com.intellij.execution.Location
import com.intellij.execution.PsiLocation
import com.intellij.execution.actions.MultipleRunLocationsProvider
import com.intellij.psi.PsiElement

class DtestMultipleRunLocationsProvider : MultipleRunLocationsProvider() {
    override fun getAlternativeLocations(originalLocation: Location<*>): List<Location<*>> {
        val psiLocation = originalLocation.toPsiLocation()
        val resolvedDtest = NavigationUtil.resolveDtest(psiLocation.psiElement)
        return if (resolvedDtest is Ok<PsiElement>) {
            listOf(PsiLocation(resolvedDtest.value))
        } else {
            emptyList()
        }
    }

    override fun getLocationDisplayName(locationCreatedFrom: Location<*>, originalLocation: Location<*>): String =
        "Duh"
}