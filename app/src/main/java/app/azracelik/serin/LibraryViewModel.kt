package app.azracelik.serin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import app.azracelik.serin.data.FavoritesStore

/** Kullanıcıya ait veriler: favoriler. */
class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val favoritesStore = FavoritesStore(application)

    val favorites = favoritesStore.ids

    fun toggleFavorite(id: String) = favoritesStore.toggle(id)
}
