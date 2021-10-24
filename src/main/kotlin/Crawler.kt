import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class Crawler(
    private val baseUri: String
) {
    private val ignoredUris = listOf("javascript:;")
    private val siteMap: MutableMap<String, Page> = mutableMapOf()
    private val visitedPages: MutableMap<String, String> = mutableMapOf()

    suspend fun crawlSite(): Map<String, Page> {
        crawlPage(dropTrailingSlash(baseUri))
        return siteMap.toMap()
    }

    private suspend fun crawlPage(url: String) {
        withContext(Dispatchers.Default) {
            if (visitedPages.containsKey(url)) return@withContext
            visitedPages[url] = ""
            withParsedPage(url) { parsedPage ->
                val links = parsedPage.getLinks()
                siteMap[url] = Page(
                    url,
                    parsedPage.title(),
                    links,
                    parsedPage.getImports()
                )
                links.map(::dropTrailingSlash)
                    .filter { it.contains(baseUri) && !it.contains('#') && !visitedPages.containsKey(it) }
                    .forEach { launch { crawlPage(it) } }
            }
        }
    }

    private suspend fun withParsedPage(url: String, function: (document: Document) -> Unit) {
        try {
            function(Jsoup.connect(url).get())
        } catch (exception: HttpStatusException) {
            println("Http exception fetching uri $url with HTTP status code ${exception.statusCode}")
            if (exception.statusCode == 429) {
                delay(500)
                withParsedPage(url, function)
            }
        }
    }

    private fun dropTrailingSlash(url: String): String = if (url.last() == '/') url.dropLast(1) else url

    private fun Document.getLinks(): List<String> =
        this.select("a[href]")
            .eachAttr("abs:href")
            .filter { !ignoredUris.contains(it) }
            .distinct()

    private fun Document.getImports(): List<String> =
        this.select("link[href]")
            .eachAttr("abs:href")
            .filter { !ignoredUris.contains(it) }
            .distinct()
}