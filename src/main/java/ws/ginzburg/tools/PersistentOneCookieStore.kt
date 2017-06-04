package ws.ginzburg.tools

import java.net.CookieStore
import java.net.HttpCookie
import java.net.URI
import java.util.prefs.Preferences

/**
 * Created by Ginzburg on 03/06/2017.
 */
class PersistentOneCookieStore : CookieStore {
    private val cookies = HashMap<String, MutableList<HttpCookie>>()
    private val preferences: Preferences = Preferences.userNodeForPackage(PersistentOneCookieStore::class.java)

    init {
        for (prefKey in preferences.keys()) {
            val newCookies = ArrayList<HttpCookie>()
            preferences.get(prefKey, "").split(";").forEach({ cookie ->
                val keyLen = cookie.indexOf("=")
                newCookies.add(HttpCookie(cookie.substring(0, keyLen), cookie.substring(keyLen + 1)))
            })
            cookies[prefKey] = newCookies
        }
    }

    fun flushURI(domain: String, value: List<HttpCookie>) {
        preferences.put(domain, value.joinToString(";"))
        preferences.flush()
    }

    fun fetchDomain(uri: URI): String {
        // the simplest approach
        var host = uri.host
        while (host.matches(Regex(".*\\..*\\..*"))) {
            host = host.substring(host.indexOf('.') + 1)
        }
        return host
    }

    override fun add(uri: URI, cookie: HttpCookie) {
        val domain = fetchDomain(uri)
        var currentCookies = cookies[domain]
        if (null == currentCookies) {
            currentCookies = ArrayList<HttpCookie>()
            cookies[domain] = currentCookies
        }
        currentCookies.add(cookie)

        flushURI(domain, currentCookies)
    }

    override fun removeAll(): Boolean {
        TODO("not implemented")
    }

    override fun getCookies(): MutableList<HttpCookie> {
        return cookies.values.flatten().toMutableList()
    }

    override fun getURIs(): List<URI> {
        return emptyList()
    }

    override fun remove(uri: URI?, cookie: HttpCookie?): Boolean {
        TODO("not implemented")
    }

    override fun get(uri: URI): List<HttpCookie> {
        return cookies[fetchDomain(uri)]?:emptyList()
    }
}