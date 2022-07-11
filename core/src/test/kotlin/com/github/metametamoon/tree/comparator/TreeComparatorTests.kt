package com.github.metametamoon.tree.comparator

import com.intellij.ide.impl.OpenProjectTask
import com.intellij.loadHeadlessAppInUnitTestMode
import com.intellij.openapi.project.ex.ProjectManagerEx
import org.junit.jupiter.api.Test
import java.io.File

class TreeComparatorTests {
    private val kotlinParserProject = run {
        loadHeadlessAppInUnitTestMode()
        ProjectManagerEx.getInstanceEx().newProject(
            File("").toPath(), OpenProjectTask(
                isNewProject = true,
            )
        )!!
    }

    @Test
    fun `compare identical trees`() {
        println("Stuff")
    }
}