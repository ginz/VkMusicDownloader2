package ws.ginzburg.vk.music2

import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.text.ParseException
import java.util.*
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

/**
 * Created by Ginzburg on 01/06/2017.
 */
class AudioList(val vkId: Int, document:Document) : ArrayList<Audio>() {
    private val VK_STR:String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMN0PQRSTUVWXYZO123456789+/="
    private val VK_STR2:String

    init {
        VK_STR2 = VK_STR + VK_STR
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
        val url = decodeVkAudioUrl(bodyNode.lastChild.attributes.getNamedItem("value").textContent)
        val labelNode = bodyNode.lastChild.previousSibling
        val artist = labelNode.firstChild.firstChild.textContent
        val title = labelNode.lastChild.firstChild.textContent
        add(Audio(url, artist, title))
    }

    // kudos to https://github.com/python273/vk_api/blob/master/vk_api/audio_url_decoder.py
    // the code is just translated from there to Kotlin without any comprehension
    fun decodeVkAudioUrl(original: String): String {
        if (!original.contains("audio_api_unavailable")) {
            return original
        }
        val vals = original.substringAfter("?extra=").split('#')
        var tStr = vkO(vals[0])
        val opsList = vkO(vals[1]).split('\t').reversed()

        for (opData in opsList) {
            val cmdWithArgs = opData.split('\u000b')

            val cmd = cmdWithArgs[0]
            val args = cmdWithArgs.drop(1)

            if (cmd == "v") tStr = tStr.reversed()
            else if (cmd == "r") tStr = vkR(tStr, args[0].toInt())
            else if (cmd == "x") tStr = vkXor(tStr, args[0])
            else if (cmd == "s") tStr = vkS(tStr, args[0].toInt())
            else if (cmd == "i") tStr = vkI(tStr, args[0].toInt())
            else throw ParseException("unknown command $cmd, please file bug report", 0)
        }

        return tStr
    }

    fun vkO(str: String):String {
        var result = ""
        var index2 = 0
        var i = 0

        for (c in str.toCharArray()) {
            val symIndex = VK_STR.indexOf(c)

            if (symIndex != -1) {
                if (index2 % 4 != 0) {
                    i = (i shl 6) + symIndex
                } else {
                    i = symIndex
                }

                if (index2 % 4 != 0) {
                    ++index2
                    val shift = (-2 * index2) and 6
                    result += (0xFF and (i shr shift)).toChar()
                } else {
                    ++index2
                }
            }
        }

        return result
    }

    fun vkR(str: String, i: Int):String {
        var result = ""

        for (c in str.toCharArray()) {
            val index = VK_STR2.indexOf(c)

            if (index != -1) {
                var offset = index - i

                if (offset < 0) {
                    offset += VK_STR2.length
                }

                result += VK_STR2[offset]
            } else {
                result += c
            }
        }

        return result
    }

    fun vkXor(str: String, i: String):String {
        val xorVal = i[0].toInt()

        return str.map({ (it.toInt() xor xorVal).toChar() }).joinToString("")
    }

    fun vkSChild(t: String, _e: Int):List<Int> {
        var i = t.length
        var e = _e

        if (i == 0) return emptyList()

        val o = mutableListOf<Int>()

        for (a in (0 until i).reversed()) {
            e = Math.abs(e) + a + i
            o += (e % i) or 0
        }

        return o.reversed()
    }

    fun vkS(t: String, e: Int):String {
        val i = t.length

        if (i == 0) return t

        val o = vkSChild(t, e)
        val tChars = t.toCharArray().toList()

        for (a in 1 until i) {
            Collections.swap(tChars, o[i - a - 1], a)
        }

        return tChars.joinToString("")
    }

    fun vkI(t: String, e: Int):String {
        return vkS(t, e xor vkId)
    }
}