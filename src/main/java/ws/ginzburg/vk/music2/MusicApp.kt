package ws.ginzburg.vk.music2

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import javafx.stage.Stage
import ws.ginzburg.tools.PersistentOneCookieStore
import java.net.CookieManager
import java.net.CookiePolicy

/**
 * Created by Ginzburg on 01/06/2017.
 */
class MusicApp : Application() {
    override fun start(stage: Stage) {
        val downloadButton = Button("Download")
        val scrollButton = Button("Scroll to the bottom")
        val vkView = WebView()

        /*CookieManager.setDefault(CookieManager(
                PersistentOneCookieStore(),
                CookiePolicy.ACCEPT_ALL))*/
        vkView.engine.load("https://m.vk.com")
        scrollButton.setOnAction {
            scrollToBottom(vkView.engine)
        }
        downloadButton.setOnAction {
            val audios = AudioList(vkView.engine.document)

            val popupStage = Stage()
            popupStage.scene = Scene(DownloadPane(audios))

            popupStage.showAndWait()
        }

        val content = VBox()
        val toolBox = HBox()

        toolBox.children.addAll(downloadButton, scrollButton)
        content.children.addAll(toolBox, vkView)

        stage.scene = Scene(content)
        stage.show()
    }

    fun scrollToBottom(engine: WebEngine) {
        engine.executeScript("window.scrollTo(0, document.body.scrollHeight);")
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(MusicApp::class.java)
        }
    }
}