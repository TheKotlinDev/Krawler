import kotlinx.serialization.Serializable

@Serializable
data class Page(
    val uri: String,
    val title: String,
    val links: List<String>,
    val resources: List<String>
)