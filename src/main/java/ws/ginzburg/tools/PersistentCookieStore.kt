package ws.ginzburg.tools

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.net.CookieManager
import java.net.CookieStore
import java.net.HttpCookie
import java.net.URI
import java.util.prefs.Preferences

/**
 * Created by Ginzburg on 03/06/2017.
 */
class PersistentCookieStore : CookieStore, Runnable {
    private val preferences: Preferences = Preferences.userNodeForPackage(PersistentCookieStore::class.java)
    private val jsonMapper = ObjectMapper()
    private val inMemoryImpl = CookieManager().cookieStore

    init {
        val oldCookiesJson = preferences["cookies", "[]"]
        val oldCookies = jsonMapper.readValue<List<SerializableCookie>>(oldCookiesJson)
        for (cookie in oldCookies) {
            add(URI.create(cookie.domain), cookie.toHttpCookie())
        }

        Runtime.getRuntime().addShutdownHook(Thread(this))
    }

    override fun run() {
        var cookies = ArrayList<SerializableCookie>()
        for (httpCookie in getCookies()) {
            cookies.add(SerializableCookie(httpCookie))
        }
        preferences.put("cookies", jsonMapper.writeValueAsString(cookies))
        preferences.flush()
    }

    override fun removeAll(): Boolean {
        return inMemoryImpl.removeAll()
    }

    override fun add(uri: URI?, cookie: HttpCookie?) {
        inMemoryImpl.add(uri, cookie)
    }

    override fun getCookies(): MutableList<HttpCookie> {
        return inMemoryImpl.cookies
    }

    override fun getURIs(): MutableList<URI> {
        return inMemoryImpl.urIs
    }

    override fun remove(uri: URI?, cookie: HttpCookie?): Boolean {
        return inMemoryImpl.remove(uri, cookie)
    }

    override fun get(uri: URI?): MutableList<HttpCookie> {
        return inMemoryImpl.get(uri)
    }

    data class SerializableCookie (
            val name: String? = null,
            val value: String? = null,
            val comment: String? = null,
            val commentUrl: String? = null,
            val domain: String? = null,
            val discard: Boolean = false,
            val path: String? = null,
            val portList: String? = null,
            val maxAge: Long = 0,
            val secure: Boolean = false,
            val version: Int = 0) {
        constructor(httpCookie: HttpCookie) : this(
                httpCookie.name,
                httpCookie.value,
                httpCookie.comment,
                httpCookie.commentURL,
                httpCookie.domain,
                httpCookie.discard,
                httpCookie.path,
                httpCookie.portlist,
                httpCookie.maxAge,
                httpCookie.secure,
                httpCookie.version)
        fun toHttpCookie(): HttpCookie {
            val httpCookie = HttpCookie(name, value)
            httpCookie.comment = comment
            httpCookie.commentURL = commentUrl
            httpCookie.domain = domain
            httpCookie.discard = discard
            httpCookie.path = path
            httpCookie.portlist = portList
            httpCookie.maxAge = maxAge
            httpCookie.secure = secure
            httpCookie.version = version
            return httpCookie
        }
    }
}