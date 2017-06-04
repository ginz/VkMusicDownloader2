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
        val xPath = XPathFactory.newInstance().newXPath()
        val infoPath = xPath.compile("//*[@class=\"ai_info\"]")
        val artistPath = xPath.compile(".//*[@class=\"ai_artist\"]")
        val titlePath = xPath.compile(".//*[@class=\"ai_title\"]")
        val urlPath = xPath.compile(".//*[@class=\"ai_body\"]/*[@type=\"hidden\"]")
        val nodeList:NodeList = infoPath.evaluate(document, XPathConstants.NODESET) as NodeList

        for (i in 0 until nodeList.length) {
            val node = nodeList.item(i)

            val audio = Audio(
                    (urlPath.evaluate(node, XPathConstants.NODE) as Node).attributes.getNamedItem("value").nodeValue,
                    (artistPath.evaluate(node, XPathConstants.NODE) as Node).firstChild.nodeValue,
                    (titlePath.evaluate(node, XPathConstants.NODE) as Node).firstChild.nodeValue
            )
            add(audio)
        }
    }
}