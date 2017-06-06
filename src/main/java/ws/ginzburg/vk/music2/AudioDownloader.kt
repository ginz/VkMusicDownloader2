package ws.ginzburg.vk.music2

import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels

/**
 * Created by Ginzburg on 04/06/2017.
 */
class AudioDownloader(val audios: List<Audio>, val directory: File, val progressReporter: (Int) -> Unit, val finishReporter: () -> Unit) {
    private @Volatile var isCanceled = false

    fun start() {
        Thread({
            for ((i, audio) in audios.withIndex()) {
                if (isCanceled) break
                val fileName = normalizeFileName(audio.artist + " - " + audio.title) + ".mp3"
                val file = File(directory, fileName)
                val audioURL = URL(audio.url)
                val channel = Channels.newChannel(audioURL.openStream()) // process exceptions!
                val outputStream = FileOutputStream(file)
                outputStream.channel.transferFrom(channel, 0, Long.MAX_VALUE)
                progressReporter(i + 1)
            }
            finishReporter()
        }).start()
    }

    fun cancel() {
        isCanceled = true
    }

    private fun normalizeFileName(fileName: String):String {
        val shortened =
                if (fileName.length > 100)
                    fileName.substring(0, 97) + "..."
                else
                    return fileName
        return shortened.replace(Regex("[^\\pL0-9.-]"), "_")
    }
}