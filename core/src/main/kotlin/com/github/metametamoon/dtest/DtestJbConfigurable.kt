package com.github.metametamoon.dtest

import com.intellij.openapi.options.Configurable
import javax.swing.*

class TextFieldWithLabel(private val labelText: String) {
    private val textField = JTextField()
    val frame = run {
        val element = JPanel()
        element.layout = BoxLayout(element, BoxLayout.X_AXIS)
        element.add(JLabel(labelText))
        element.add(textField)
        element
    }
    val text: String
        get() = textField.text
}

class DtestJbConfigurable() : Configurable {
    private val settingsFrame by lazy { JPanel() }
    private val pathToSourceFolderPanel by lazy { TextFieldWithLabel("Path to source folder:") }
    private val pathToGenerationFolderPanel by lazy { TextFieldWithLabel("Path to generation directory:") }
    private val pathToSettingsPanel by lazy { TextFieldWithLabel("Path to settings:") }

    init {
        settingsFrame.layout = BoxLayout(settingsFrame, BoxLayout.Y_AXIS)
        settingsFrame.add(pathToSourceFolderPanel.frame)
        settingsFrame.add(pathToGenerationFolderPanel.frame)
        settingsFrame.add(pathToSettingsPanel.frame)
    }

    override fun createComponent(): JComponent = settingsFrame

    override fun isModified(): Boolean {
        return !(pathToSettingsPanel.text == DtestJbSettings.getInstance().pathToSettings &&
                pathToSourceFolderPanel.text == DtestJbSettings.getInstance().pathToSourceFolder &&
                pathToGenerationFolderPanel.text == DtestJbSettings.getInstance().pathToGenerationFolder)
    }

    override fun apply() {
        DtestJbSettings.getInstance().pathToSettings = pathToSettingsPanel.text
        DtestJbSettings.getInstance().pathToSourceFolder = pathToSourceFolderPanel.text
        DtestJbSettings.getInstance().pathToGenerationFolder = pathToGenerationFolderPanel.text
    }

    override fun getDisplayName(): String = "Dtest Settings"
}