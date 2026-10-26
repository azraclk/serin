package app.azracelik.serin.playback

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import app.azracelik.serin.R
import app.azracelik.serin.data.HistoryStore
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File

private const val AudioCacheBytes = 300L * 1024 * 1024
private const val ExtendMinutes = 5
private const val ExtendAction = "app.azracelik.serin.EXTEND_SESSION"
private const val EndAction = "app.azracelik.serin.END_SESSION"

/**
 * Meditasyon seslerini çalar. Ekran kapanınca da çalmaya devam eder; kilit ekranında ve bildirimde
 * kontroller görünür. Dinlenen sesler önbelleğe alınır, tekrar dinlemek için internet gerekmez.
 */
@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private val scope = MainScope()
    private var sessionTimer: SessionTimer? = null

    override fun onCreate() {
        super.onCreate()
        val dataSourceFactory = CacheDataSource.Factory()
            .setCache(audioCache(this))
            .setUpstreamDataSourceFactory(DefaultDataSource.Factory(this))
        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ true,
            )
            // Kulaklık çıkarılınca duraklat.
            .setHandleAudioBecomingNoisy(true)
            .build()
        val history = HistoryStore.get(this)
        sessionTimer = SessionTimer(player, scope) { mediaId, firstMinute ->
            if (mediaId != null) history.addMinute(mediaId, firstMinute)
        }.also { it.start() }
        val sessionPlayer = SessionPlayer(player, this)
        val extend = SessionCommand(ExtendAction, Bundle.EMPTY)
        val end = SessionCommand(EndAction, Bundle.EMPTY)
        mediaSession = MediaSession.Builder(this, sessionPlayer)
            .setCallback(SessionCallback(extend, end))
            .setMediaButtonPreferences(
                ImmutableList.of(
                    CommandButton.Builder(CommandButton.ICON_PLUS)
                        .setSessionCommand(extend)
                        .setDisplayName(getString(R.string.session_extend, ExtendMinutes))
                        .build(),
                    CommandButton.Builder(CommandButton.ICON_STOP)
                        .setSessionCommand(end)
                        .setDisplayName(getString(R.string.session_end))
                        .build(),
                ),
            )
            .build()
        // Süre ya da kalan dakika değişince bildirim, oynatıcıda olay olmasa da yenilenir.
        scope.launch {
            MeditationSession.state
                .map { it.lengthMs to it.remainingMinutesCeil }
                .distinctUntilChanged()
                .collect { sessionPlayer.invalidate() }
        }
    }

    private inner class SessionCallback(
        private val extend: SessionCommand,
        private val end: SessionCommand,
    ) : MediaSession.Callback {
        override fun onConnect(session: MediaSession, controller: MediaSession.ControllerInfo): MediaSession.ConnectionResult {
            val commands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon().add(extend).add(end).build()
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(commands)
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle,
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                ExtendAction -> MeditationSession.extend(ExtendMinutes)
                EndAction -> session.player.run {
                    // Mini player ve bildirim kaybolur; uygulamadaki "bitir" ile aynı.
                    stop()
                    clearMediaItems()
                }
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Uygulama kapatıldığında çalan bir şey yoksa servisi de kapat.
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        sessionTimer?.release()
        scope.cancel()
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }
}

@UnstableApi
private var cache: SimpleCache? = null

/** Aynı klasörde tek bir SimpleCache olabilir; servis yeniden oluşturulsa da aynısı kullanılır. */
@OptIn(UnstableApi::class)
@Synchronized
private fun audioCache(context: Context): SimpleCache = cache ?: SimpleCache(
    File(context.cacheDir, "audio"),
    LeastRecentlyUsedCacheEvictor(AudioCacheBytes),
    StandaloneDatabaseProvider(context),
).also { cache = it }
