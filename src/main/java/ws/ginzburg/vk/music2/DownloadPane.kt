package ws.ginzburg.vk.music2

import javafx.application.Platform
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import java.io.File

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
            val progressIndicator = ProgressIndicator()
            progressIndicator.progress = .0
            children.add(progressIndicator)
            content.isDisable = true
            AudioDownloader(selectedAudios, File(directoryInput.text), { i ->
                Platform.runLater({
                    progressIndicator.progress = i * 1.0 / selectedAudios.size
                })
            }, {
                Platform.runLater({
                    children.remove(progressIndicator)
                    content.isDisable = false
                })
            }).start()
        }

        children.add(content)
    }
}