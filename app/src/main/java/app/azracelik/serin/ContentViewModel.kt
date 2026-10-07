package app.azracelik.serin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.azracelik.serin.data.ContentRepository
import app.azracelik.serin.ui.markdown.MdBlock
import app.azracelik.serin.ui.markdown.parseMarkdown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File

private const val HttpCacheBytes = 20L * 1024 * 1024

class ContentViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ContentRepository(
        application,
        OkHttpClient.Builder()
            .cache(Cache(File(application.cacheDir, "http"), HttpCacheBytes))
            .build(),
    )

    val content = repository.content

    init {
        viewModelScope.launch { repository.refresh() }
    }

    /** Yazıyı indirip ayrıştırır; indirilemezse null döner. */
    suspend fun loadPost(url: String): List<MdBlock>? =
        repository.loadText(url)?.let { withContext(Dispatchers.Default) { parseMarkdown(it) } }
}
