package app.azracelik.serin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import app.azracelik.serin.data.AudioStatus
import app.azracelik.serin.data.AudioStore
import app.azracelik.serin.data.BreathingPattern
import app.azracelik.serin.data.BreathingPatternStore
import app.azracelik.serin.data.FavoritesStore
import app.azracelik.serin.data.HistoryStore
import app.azracelik.serin.data.Meditation

/** Kullanıcıya ait veriler: favoriler, tamamlanan seanslar ve indirilen sesler. */
class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val favoritesStore = FavoritesStore(application)
    private val audioStore = AudioStore.get(application)
    private val breathingStore = BreathingPatternStore(application)

    val favorites = favoritesStore.ids

    val history = HistoryStore.get(application).records

    val downloads = audioStore.status

    val breathingPattern = breathingStore.pattern

    fun selectBreathingPattern(pattern: BreathingPattern) = breathingStore.select(pattern)

    val breathingHaptics = breathingStore.haptics

    fun setBreathingHaptics(enabled: Boolean) = breathingStore.setHaptics(enabled)

    fun toggleFavorite(id: String) = favoritesStore.toggle(id)

    /** İndirilmemişse (ya da indirme başarısızsa) indirir, indirilmişse telefondan kaldırır. */
    fun toggleDownload(meditation: Meditation) {
        val url = meditation.audio ?: return
        when (downloads.value[AudioStore.key(meditation.id, url)]) {
            AudioStatus.Downloaded -> audioStore.remove(meditation.id, url)
            AudioStatus.Downloading -> Unit
            AudioStatus.Failed, null -> audioStore.startDownload(meditation.id, url)
        }
    }
}
