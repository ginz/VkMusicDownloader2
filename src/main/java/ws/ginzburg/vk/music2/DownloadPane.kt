package ws.ginzburg.vk.music2

import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.io.File
import java.util.concurrent.FutureTask

/**
 * Created by Ginzburg on 01/06/2017.
 */
class DownloadPane(audios: List<Audio>) : StackPane() {
    init {
        val selected = HashMap<Audio, Boolean>()

        val foundLabel = Label("${audios.size} audio tracks found on the page:")

        val directoryLabel = Label("Choose directory to download to:")
        val directoryInput = TextField()
        val browseButton = Button("Browse")
        browseButton.setOnAction {
            val directoryChooser = DirectoryChooser()
            directoryChooser.title = "Download directory"
            val directory = directoryChooser.showDialog(scene.window)
            if (directory != null) {
                directoryInput.text = directory.toString()
            }
        }
        var directoryLine = HBox()
        directoryLine.children.addAll(directoryLabel, directoryInput, browseButton)

        val selectButton = Button("Select all")
        val deselectButton = Button("Deselect all")
        val downloadButton = Button("Download")

        val toolBox = HBox()
        toolBox.children.addAll(selectButton, deselectButton, downloadButton)

        val checkBoxes = ArrayList<CheckBox>()
        val audiosGrid = GridPane()
        audios.forEachIndexed({ index, audio ->
            val downloadCheckBox = CheckBox()
            downloadCheckBox.isSelected = true
            downloadCheckBox.selectedProperty().addListener({ _, _, new ->
                selected[audio] = new
            })
            checkBoxes.add(downloadCheckBox)

            audiosGrid.add(downloadCheckBox, 0, index)
            audiosGrid.add(Label(audio.artist), 1, index)
            audiosGrid.add(Label(audio.title), 2, index)
        })
        val audiosScrollPane = ScrollPane()
        audiosScrollPane.content = audiosGrid
        audiosScrollPane.vbarPolicy = ScrollPane.ScrollBarPolicy.ALWAYS
        audiosScrollPane.prefHeight = 200.0

        selectButton.setOnAction {
            for (checkBox in checkBoxes) checkBox.isSelected = true
        }
        deselectButton.setOnAction {
            for (checkBox in checkBoxes) checkBox.isSelected = false
        }

        val content = VBox()
        content.children.addAll(foundLabel, directoryLine, toolBox, audiosScrollPane)

        downloadButton.setOnAction {
            val selectedAudios = ArrayList<Audio>()
            for (audio in audios) {
                if (audio !in selected || selected[audio]!!) selectedAudios.add(audio)
            }

            if (!File(directoryInput.text).isDirectory) {
                val alert = Alert(Alert.AlertType.WARNING)
                alert.title = "Destination doesn't exist"
                alert.headerText = null
                alert.contentText = "The destination directory you've chosen doesn't exist or is not file"
                alert.showAndWait()
                return@setOnAction
            }
            if (selectedAudios.isEmpty()) {
                val alert = Alert(Alert.AlertType.WARNING)
                alert.title = "0 audios selected"
                alert.headerText = null
                alert.contentText = "You haven't selected no audio tracks to download"
                alert.showAndWait()
                return@setOnAction
            }

            val progressIndicator = ProgressPane()
            progressIndicator.progress = 0.0
            val progressStage = Stage()
            progressStage.scene = Scene(progressIndicator, 300.0, 70.0)
            progressStage.initModality(Modality.APPLICATION_MODAL)
            progressStage.initStyle(StageStyle.UTILITY)
            progressStage.initOwner(scene.window)
            progressStage.show()
            content.isDisable = true
            val audioDownloader = AudioDownloader(selectedAudios, File(directoryInput.text), { i ->
                progressIndicator.progress = i * 1.0 / selectedAudios.size
            }, {
                Platform.runLater({
                    progressStage.close()
                    content.isDisable = false
                })
            }, { audio, ex ->
                val cancelButton = ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE)
                val skipButton = ButtonType("Skip", ButtonBar.ButtonData.NEXT_FORWARD)
                val retryButton = ButtonType("Retry", ButtonBar.ButtonData.BACK_PREVIOUS)

                val futureTask = FutureTask({
                    val alert = Alert(Alert.AlertType.ERROR,
                            "Failed to download audio: ${ex.javaClass.simpleName} (${ex.message})",
                            cancelButton, skipButton, retryButton)

                    alert.title = "Audio download has failed"
                    alert.showAndWait().orElse(skipButton)
                })
                Platform.runLater(futureTask)
                val result = futureTask.get()

                if (result == cancelButton) ErrorResponse.CANCEL
                else if (result == skipButton) ErrorResponse.SKIP
                else ErrorResponse.RETRY
            })
            audioDownloader.start()
            progressIndicator.cancelListener = {
                audioDownloader.cancel()
            }
        }

        children.add(content)
    }
}