package app.azracelik.serin.data

import kotlinx.serialization.Serializable

/** serin-content reposundaki content.json dosyasının karşılığı. */
@Serializable
data class SerinContent(
    val home: HomeContent,
    val meditations: List<Meditation>,
    val blogPosts: List<BlogPost>,
) {
    val featuredPosts: List<BlogPost>
        get() = home.featuredPosts.mapNotNull { id -> blogPosts.find { it.id == id } }

    fun meditation(id: String): Meditation? = meditations.find { it.id == id }

    fun blogPost(id: String): BlogPost? = blogPosts.find { it.id == id }

    /** JSON'daki dosya yollarını (görsel, ses, yazı) [baseUrl] ile tam adrese çevirir. */
    fun resolveUrls(baseUrl: String) = copy(
        home = home.copy(banner = baseUrl + home.banner),
        meditations = meditations.map {
            it.copy(image = baseUrl + it.image, audio = it.audio?.let { audio -> baseUrl + audio })
        },
        blogPosts = blogPosts.map {
            it.copy(image = baseUrl + it.image, body = it.body?.let { body -> baseUrl + body })
        },
    )
}

@Serializable
data class HomeContent(
    val banner: String,
    val featuredPosts: List<String>,
)

@Serializable
data class Meditation(
    val id: String,
    val label: String,
    val title: String,
    val image: String,
    /** Sesi olmayan meditasyonlarda play butonu pasif görünür. */
    val audio: String? = null,
)

@Serializable
data class BlogPost(
    val id: String,
    val title: String,
    val image: String,
    /** Yazının Markdown dosyası; yoksa detay ekranında "yakında" yazar. */
    val body: String? = null,
)
