package app.azracelik.serin.data

import androidx.core.content.edit
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val PrefsName = "serin_library"
private const val FavoritesKey = "favorite_ids"

/** [id] kümedeyse çıkarır, değilse ekler. */
fun Set<String>.toggled(id: String): Set<String> = if (id in this) this - id else this + id

/**
 * Kullanıcının favori meditasyonları. Yalnızca id'ler saklanır; içerikten kaldırılan bir meditasyon
 * listede görünmez, içeriğe geri dönerse yeniden favori olarak çıkar.
 */
class FavoritesStore(context: Context) {
    private val prefs = context.getSharedPreferences(PrefsName, Context.MODE_PRIVATE)

    // getStringSet'in döndürdüğü küme değiştirilmemeli; kopyası alınır.
    private val _ids = MutableStateFlow(prefs.getStringSet(FavoritesKey, null)?.toSet().orEmpty())
    val ids: StateFlow<Set<String>> = _ids.asStateFlow()

    fun toggle(id: String) {
        _ids.update { it.toggled(id) }
        prefs.edit { putStringSet(FavoritesKey, _ids.value) }
    }
}
