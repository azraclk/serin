package app.azracelik.serin.playback

import android.content.Context
import android.content.Intent
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
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import java.io.File

private const val AudioCacheBytes = 300L * 1024 * 1024

/**
 * Meditasyon seslerini çalar. Ekran kapanınca da çalmaya devam eder; kilit ekranında ve bildirimde
 * kontroller görünür. Dinlenen sesler önbelleğe alınır, tekrar dinlemek için internet gerekmez.
 */
@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

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
        mediaSession = MediaSession.Builder(this, player).build()
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
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }
}

@OptIn(UnstableApi::class)
private var cache: SimpleCache? = null

/** Aynı klasörde tek bir SimpleCache olabilir; servis yeniden oluşturulsa da aynısı kullanılır. */
@OptIn(UnstableApi::class)
@Synchronized
private fun audioCache(context: Context): SimpleCache = cache ?: SimpleCache(
    File(context.cacheDir, "audio"),
    LeastRecentlyUsedCacheEvictor(AudioCacheBytes),
    StandaloneDatabaseProvider(context),
).also { cache = it }
