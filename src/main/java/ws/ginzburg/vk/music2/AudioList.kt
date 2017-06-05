package ws.ginzburg.vk.music2

import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

/**
 * Created by Ginzburg on 01/06/2017.
 */
class AudioList(document:Document) : ArrayList<Audio>() {
    init {
        findAudios(document)
    }

    fun findAudios(node: Node) {
        if (node.nodeName == "DIV" && node.attributes.getNamedItem("class")?.textContent == "ai_info") {
            processAudioNode(node)
            return
        }
        val childNodes = node.childNodes
        for (i in 0 until childNodes.length) {
            findAudios(childNodes.item(i))
        }
    }

    fun processAudioNode(node: Node) {
        // hardcode paths to work faster
        val bodyNode = node.lastChild
        val url = bodyNode.lastChild.attributes.getNamedItem("value").textContent
        val labelNode = bodyNode.lastChild.previousSibling
        val artist = labelNode.firstChild.firstChild.textContent
        val title = labelNode.lastChild.firstChild.textContent
        add(Audio(url, artist, title))
    }
}