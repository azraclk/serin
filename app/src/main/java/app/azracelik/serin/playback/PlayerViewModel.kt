package app.azracelik.serin.playback

import android.app.Application
import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import app.azracelik.serin.data.Meditation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

private const val PrefsName = "player"
private const val LengthKey = "session_length_minutes"
private const val LastMeditationKey = "last_meditation_id"

data class PlaybackState(
    /** Seansı süren (çalan ya da duraklatılmış) meditasyonun id'si. */
    val mediaId: String? = null,
    val isPlaying: Boolean = false,
    val session: SessionState = MeditationSession.state.value,
)

/** Ekranlarla [PlaybackService] arasındaki köprü. */
class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences(PrefsName, Context.MODE_PRIVATE)
    private val controllerFuture = MediaController.Builder(
        application,
        SessionToken(application, ComponentName(application, PlaybackService::class.java)),
    ).buildAsync()
    private var controller: MediaController? = null

    private val player = MutableStateFlow(PlaybackState())

    /** En son çalınan meditasyonun id'si; uygulama kapanıp açılsa da korunur (ana sayfada "devam et"). */
    private val _lastMeditationId = MutableStateFlow(prefs.getString(LastMeditationKey, null))
    val lastMeditationId: StateFlow<String?> = _lastMeditationId

    val state: StateFlow<PlaybackState> = combine(player, MeditationSession.state) { player, session ->
        player.copy(session = session)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, PlaybackState())

    init {
        MeditationSession.setLength(prefs.getInt(LengthKey, MeditationSession.DefaultLengthMinutes))
        controllerFuture.addListener(
            {
                controller = controllerFuture.get().also { controller ->
                    controller.addListener(object : Player.Listener {
                        override fun onEvents(player: Player, events: Player.Events) = updateState()
                    })
                }
                updateState()
            },
            ContextCompat.getMainExecutor(application),
        )
    }

    fun setSessionLength(minutes: Int) {
        MeditationSession.setLength(minutes)
        prefs.edit { putInt(LengthKey, minutes) }
    }

    /** Bu meditasyon çalıyorsa duraklatır, değilse çalmaya başlar. */
    fun togglePlayback(meditation: Meditation) {
        val controller = controller ?: return
        val audio = meditation.audio ?: return
        if (controller.currentMediaItem?.mediaId == meditation.id) {
            if (controller.isPlaying) controller.pause() else controller.play()
            return
        }
        val item = MediaItem.Builder()
            .setMediaId(meditation.id)
            .setUri(audio)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(meditation.title)
                    .setArtist("serin")
                    .setArtworkUri(meditation.image.toUri())
                    .build(),
            )
            .build()
        controller.setMediaItem(item)
        controller.prepare()
        controller.play()
        _lastMeditationId.value = meditation.id
        prefs.edit { putString(LastMeditationKey, meditation.id) }
    }

    /** Çalan seansın oynat/duraklat düğmesi (mini player). */
    fun toggleCurrent() {
        val controller = controller ?: return
        if (controller.isPlaying) controller.pause() else controller.play()
    }

    /** Seansı bitirir; mini player kaybolur. */
    fun stop() {
        val controller = controller ?: return
        controller.stop()
        controller.clearMediaItems()
    }

    private fun updateState() {
        val controller = controller ?: return
        player.value = PlaybackState(
            mediaId = controller.currentMediaItem?.mediaId,
            isPlaying = controller.isPlaying,
        )
    }

    override fun onCleared() {
        MediaController.releaseFuture(controllerFuture)
    }
}
