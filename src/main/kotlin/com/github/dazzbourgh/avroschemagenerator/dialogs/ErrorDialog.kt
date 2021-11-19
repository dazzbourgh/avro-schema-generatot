package com.github.dazzbourgh.avroschemagenerator.dialogs

import com.intellij.openapi.ui.DialogWrapper
import java.awt.BorderLayout
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class ErrorDialog(private val message: String) : DialogWrapper(true) {
    init {
        title = "Error"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val dialogPanel = JPanel(BorderLayout())

        val label = JLabel(message)
        dialogPanel.add(label, BorderLayout.CENTER)

        return dialogPanel
    }

    override fun createActions(): Array<Action> {
        return arrayOf(myOKAction)
    }
}