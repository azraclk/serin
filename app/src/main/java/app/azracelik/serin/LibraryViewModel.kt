package app.azracelik.serin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import app.azracelik.serin.data.FavoritesStore
import app.azracelik.serin.data.HistoryStore

/** Kullanıcıya ait veriler: favoriler ve tamamlanan seanslar. */
class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val favoritesStore = FavoritesStore(application)

    val favorites = favoritesStore.ids

    val history = HistoryStore.get(application).records

    fun toggleFavorite(id: String) = favoritesStore.toggle(id)
}
