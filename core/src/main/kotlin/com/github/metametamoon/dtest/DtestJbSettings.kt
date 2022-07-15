package com.github.metametamoon.dtest

import com.intellij.ide.util.PropertiesComponent
import kotlin.reflect.KProperty

object PropertyComponentDelegate {
    private const val dtestPrefix = "com.github.metametamoon.dtest"
    operator fun getValue(thisRef: Any, property: KProperty<*>): String {
        val lookupName = dtestPrefix + property.name
        return PropertiesComponent.getInstance().getValue(lookupName).orEmpty()
    }

    operator fun setValue(thisRef: Any, property: KProperty<*>, value: String) {
        val lookupName = dtestPrefix + property.name
        PropertiesComponent.getInstance().setValue(lookupName, value)
    }
}

class DtestJbSettings private constructor(
) {
    var pathToSourceFolder: String by PropertyComponentDelegate
    var pathToGenerationFolder: String by PropertyComponentDelegate
    var pathToSettings: String by PropertyComponentDelegate

    companion object {
        private val instance = DtestJbSettings()
        fun getInstance(): DtestJbSettings = instance
    }
}