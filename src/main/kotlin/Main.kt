import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

fun main() = runBlocking {
    val crawler = Crawler("https://thekotlindev.com/")
    val siteMap = crawler.crawlSite()
    println("found ${siteMap.size} pages for site")
    File("crawledSite.json").writeText(Json.encodeToString(siteMap))
}

