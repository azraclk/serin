package app.azracelik.serin.playback

import android.app.Application
import android.content.ComponentName
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import app.azracelik.serin.data.Meditation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlaybackState(
    /** Çalan ya da duraklatılmış meditasyonun id'si. */
    val mediaId: String? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
) {
    val progress: Float
        get() = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
}

/** Ekranlarla [PlaybackService] arasındaki köprü. */
class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val controllerFuture = MediaController.Builder(
        application,
        SessionToken(application, ComponentName(application, PlaybackService::class.java)),
    ).buildAsync()
    private var controller: MediaController? = null

    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    init {
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
        // İlerleme çubuğu için çalarken konumu düzenli olarak güncelle.
        viewModelScope.launch {
            while (isActive) {
                if (controller?.isPlaying == true) updateState()
                delay(500)
            }
        }
    }

    /** Bu meditasyon çalıyorsa duraklatır, değilse çalmaya başlar. */
    fun togglePlayback(meditation: Meditation) {
        val controller = controller ?: return
        val audio = meditation.audio ?: return
        if (controller.currentMediaItem?.mediaId == meditation.id) {
            when {
                controller.isPlaying -> controller.pause()
                controller.playbackState == Player.STATE_ENDED -> {
                    controller.seekTo(0)
                    controller.play()
                }
                else -> controller.play()
            }
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
    }

    private fun updateState() {
        val controller = controller ?: return
        _state.value = PlaybackState(
            mediaId = controller.currentMediaItem?.mediaId,
            isPlaying = controller.isPlaying,
            positionMs = controller.currentPosition,
            durationMs = controller.duration.coerceAtLeast(0),
        )
    }

    override fun onCleared() {
        MediaController.releaseFuture(controllerFuture)
    }
}
