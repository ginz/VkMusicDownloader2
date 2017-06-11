package ws.ginzburg.vk.music2

import javafx.application.Platform
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.layout.VBox

/**
 * Created by Ginzburg on 07/06/2017.
 */
class ProgressPane(var cancelListener: (() -> Unit)? = null) : VBox() {
    val progressBar = ProgressBar()
    var progress = 0.0
    set(value) {
        field = value
        Platform.runLater({
            progressBar.progress = value
        })
    }

    init {
        val headerLabel = Label("Downloading:")
        val cancelButton = Button("Cancel")

        cancelButton.setOnAction {
            headerLabel.text = "Cancelling..."
            progressBar.isVisible = false
            cancelListener?.invoke()
        }

        progressBar.prefWidthProperty().bind(widthProperty())

        children.addAll(headerLabel, progressBar, cancelButton)
    }
}