package com.github.metametamoon.dtest

class DtestJbSettings private constructor() {
    companion object {
        private val instance = DtestJbSettings()
        fun getInstance(): DtestJbSettings = instance
    }

    var pathToSourceFolder: String = ""
    var pathToGenerationFolder: String = ""
    var pathToSettings: String = ""
}