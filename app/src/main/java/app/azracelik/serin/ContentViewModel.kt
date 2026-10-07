package app.azracelik.serin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.azracelik.serin.data.ContentRepository
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

class ContentViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ContentRepository(application, OkHttpClient())

    val content = repository.content

    init {
        viewModelScope.launch { repository.refresh() }
    }
}
