import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CrawlerTest {

    private val crawler = Crawler("https://example.com")

    @BeforeEach
    fun setUp() {
        mockkStatic(Jsoup::class)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Jsoup::class)
    }

    @Test
    fun `can crawl website with single page`() = runBlocking {
        pageExists(
            "https://example.com",
            "A page title",
            emptyList(),
            listOf("https://import.com")
        )

        val crawledSite = crawler.crawlSite()

        assertEquals(1, crawledSite.size)
    }

    @Test
    fun `can crawl website with many pages`() = runBlocking {
        pageExists(
            "https://example.com",
            "A page title",
            listOf("https://example.com/about"),
            listOf("https://import.com")
        )
        pageExists(
            "https://example.com/about",
            "About page",
            emptyList(),
            listOf("https://example.com/css/styles.css")
        )

        val crawledSite = crawler.crawlSite()

        assertEquals(2, crawledSite.size)
    }

    @Test
    fun `handles cyclic links`() = runBlocking {
        pageExists(
            "https://example.com",
            "A page title",
            listOf("https://example.com/about", "https://example.com/faq"),
            listOf("https://import.com")
        )
        pageExists(
            "https://example.com/about",
            "About page",
            listOf("https://example.com"),
            listOf("https://example.com/css/styles.css")
        )
        pageExists(
            "https://example.com/faq",
            "About page",
            listOf("https://example.com/about", "https://example.com"),
            listOf("https://example.com/css/styles.css")
        )

        val crawledSite = crawler.crawlSite()

        assertEquals(3, crawledSite.size)
    }

    @Test
    fun `recognises parent pages and ignores others`() = runBlocking {
        pageExists(
            "https://example.com",
            "A page title",
            listOf("https://example.com/about"),
            listOf("https://import.com")
        )
        pageExists(
            "https://example.com/about",
            "About page",
            listOf("https://example.com"),
            listOf("https://example.com/css/styles.css")
        )
        pageExists(
            "https://example.com/about/#history",
            "About page",
            listOf("https://example.com"),
            listOf("https://example.com/css/styles.css")
        )
        pageExists(
            "https://example.com/about/",
            "About page",
            listOf("https://example.com"),
            listOf("https://example.com/css/styles.css")
        )

        val crawledSite = crawler.crawlSite()

        assertEquals(2, crawledSite.size)
    }

    @Test
    fun `only crawls pages on same base uri`() = runBlocking {
        pageExists(
            "https://example.com",
            "A page title",
            listOf("https://anotherSite.com/", "https://google.com"),
            listOf("https://import.com")
        )

        val crawledSite = crawler.crawlSite()

        assertEquals(1, crawledSite.size)
    }

    @Test
    fun `only crawls pages where a link exists`() = runBlocking {
        pageExists(
            "https://example.com",
            "A page title",
            emptyList(),
            listOf("https://import.com")
        )
        pageExists(
            "https://example.com/about",
            "About page",
            listOf("https://example.com"),
            listOf("https://example.com/css/styles.css")
        )
        pageExists(
            "https://example.com/faq",
            "About page",
            listOf("https://example.com/about", "https://example.com"),
            listOf("https://example.com/css/styles.css")
        )

        val crawledSite = crawler.crawlSite()

        assertEquals(1, crawledSite.size)
    }

    private fun pageExists(
        url: String,
        title: String,
        links: List<String>,
        imports: List<String>
    ) {
        val mockConnection = mockk<Connection>()
        every { Jsoup.connect(url) } returns mockConnection
        val mockDocument = mockk<Document>()
        every { mockConnection.get() } returns mockDocument
        every { mockDocument.title() } returns title

        val mockLinks = mockk<Elements>()
        every { mockDocument.select("a[href]") } returns mockLinks
        every { mockLinks.eachAttr("abs:href") } returns links
        val mockImports = mockk<Elements>()
        every { mockDocument.select("link[href]") } returns mockLinks
        every { mockImports.eachAttr("abs:href") } returns imports
    }
}