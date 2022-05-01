package com.github.metametamoon.dtest.services

import com.github.metametamoon.dtest.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
