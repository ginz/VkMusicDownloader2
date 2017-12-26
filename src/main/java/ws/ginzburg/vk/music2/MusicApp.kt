package ws.ginzburg.vk.music2

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.TextInputDialog
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import javafx.stage.Stage
import ws.ginzburg.tools.PersistentCookieStore
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.prefs.Preferences

/**
 * Created by Ginzburg on 01/06/2017.
 */
class MusicApp : Application() {
    private val preferences: Preferences = Preferences.userNodeForPackage(MusicApp::class.java)

    override fun start(stage: Stage) {
        val downloadButton = Button("Download")
        val scrollButton = Button("Scroll to the bottom")
        val refreshButton = Button("Refresh")
        val setProxyButton = Button("Set proxy")
        val vkView = WebView()
        CookieManager.setDefault(CookieManager(
                PersistentCookieStore(),
                CookiePolicy.ACCEPT_ALL))
        loadProxy()
        vkView.engine.load("https://m.vk.com/audio")
        scrollButton.setOnAction {
            scrollToBottom(vkView.engine)
        }
        downloadButton.setOnAction {
            val vkId = vkView.engine.executeScript("vk.id")
            if (vkId == null || vkId !is Int) {
                var alert = Alert(Alert.AlertType.WARNING)
                alert.title = "VK User not found"
                alert.contentText = "Please open an issue at project's GitHub page"
                alert.showAndWait()
                return@setOnAction
            }
            val audios = AudioList(vkId, vkView.engine.document)

            if (audios.isEmpty()) {
                val alert = Alert(Alert.AlertType.WARNING)
                alert.title = "Nothing to download"
                alert.headerText = "No audio elements found on the current page"
                alert.contentText = "You are expected to proceed to a page with audio elements, such as your music page or your feed"
                alert.showAndWait()

                return@setOnAction
            }

            val popupStage = Stage()
            popupStage.scene = Scene(DownloadPane(audios))

            popupStage.showAndWait()
        }
        refreshButton.setOnAction {
            vkView.engine.reload()
        }
        setProxyButton.setOnAction {
            val proxyInput = TextInputDialog(getProxy())
            proxyInput.title = "Set proxy"
            proxyInput.headerText = "Set proxy host and port"
            proxyInput.contentText = "Input proxy host and port in format host:port, for example, myproxy.ru:8080 (empty for no proxy)"

            val result = proxyInput.showAndWait()
            result.ifPresent({ proxy ->
                var host = ""
                var port: Int? = 0
                if (!proxy.isEmpty()) {
                    var failedToParse = false
                    val delimiterIndex = proxy.lastIndexOf(":")
                    if (delimiterIndex == -1) {
                        failedToParse = true
                    }
                    else {
                        port = proxy.substring(delimiterIndex + 1).toIntOrNull()
                        if (port == null) failedToParse = true
                        else {
                            host = proxy.substring(0, delimiterIndex)
                        }
                    }
                    if (failedToParse) {
                        val alert = Alert(Alert.AlertType.WARNING)
                        alert.title = "Incorrect proxy format"
                        alert.contentText = "Please use host:port format"

                        alert.showAndWait()
                        return@ifPresent
                    }
                    setProxy(host, port!!)
                    saveProxy(host, port)
                    vkView.engine.reload()
                }
            })
        }

        val content = VBox()
        val toolBox = HBox()

        toolBox.children.addAll(downloadButton, scrollButton, refreshButton, setProxyButton)
        content.children.addAll(toolBox, vkView)

        stage.scene = Scene(content)
        stage.show()
    }

    fun setProxy(host: String, port: Int) {
        System.setProperty("https.proxyHost", host)
        System.setProperty("https.proxyPort", port.toString())
    }

    fun loadProxy() {
        setProxy(preferences["proxyHost", ""], preferences.getInt("proxyPort", 0))
    }

    fun saveProxy(host: String, port: Int) {
        preferences.put("proxyHost", host)
        preferences.put("proxyPort", port.toString())
    }

    fun getProxy() =
        if (preferences.get("proxyHost", "").isEmpty()) ""
        else preferences.get("proxyHost", "") + ":" + preferences.getInt("proxyPort", 0)

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