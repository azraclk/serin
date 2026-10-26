package app.azracelik.serin.playback

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.FlagSet
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import app.azracelik.serin.R

/**
 * Bildirimin ve kilit ekranının gördüğü oynatıcı. Ses döngüde çalındığı için kendi süresi ve konumu seansı
 * anlatmaz; burada seçilen seans süresi ve geçen süre bildirilir, böylece ilerleme çubuğu seansla birlikte
 * dolar. Seans içinde atlamanın anlamı olmadığından ileri/geri sarma komutları kapalıdır.
 */
internal class SessionPlayer(player: Player, private val context: Context) : ForwardingPlayer(player) {
    private val seekCommands = intArrayOf(
        Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM,
        Player.COMMAND_SEEK_TO_PREVIOUS,
        Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
        Player.COMMAND_SEEK_TO_NEXT,
        Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
    )
    private val listeners = mutableListOf<Player.Listener>()

    override fun addListener(listener: Player.Listener) {
        listeners += listener
        super.addListener(listener)
    }

    override fun removeListener(listener: Player.Listener) {
        listeners -= listener
        super.removeListener(listener)
    }

    override fun getAvailableCommands(): Player.Commands =
        Player.Commands.Builder().addAll(super.getAvailableCommands()).removeAll(*seekCommands).build()

    override fun isCommandAvailable(command: Int): Boolean =
        command !in seekCommands && super.isCommandAvailable(command)

    override fun getDuration(): Long = sessionLength()

    override fun getContentDuration(): Long = sessionLength()

    override fun getCurrentPosition(): Long = elapsed()

    override fun getContentPosition(): Long = elapsed()

    // Seans süresi boyunca ses zaten hazır (döngüde); çubukta "yükleniyor" gölgesi çıkmasın.
    override fun getBufferedPosition(): Long = sessionLength().takeIf { it != C.TIME_UNSET } ?: 0L

    override fun getContentBufferedPosition(): Long = bufferedPosition

    /** Bildirimde sanatçı satırı yerine seansın kalan süresi görünür. */
    override fun getMediaMetadata(): MediaMetadata {
        val remaining = context.getString(R.string.session_remaining, MeditationSession.state.value.remainingMinutesCeil)
        return super.getMediaMetadata().buildUpon().setArtist("serin · $remaining").build()
    }

    /**
     * Seans süresi ya da kalan dakika, oynatıcıda bir olay olmadan değiştiğinde bildirimin yeniden okuması için
     * dinleyicilere haber verir.
     */
    fun invalidate() {
        val position = Player.PositionInfo(
            /* windowUid = */ null, currentMediaItemIndex, currentMediaItem, /* periodUid = */ null,
            currentPeriodIndex, currentPosition, contentPosition, C.INDEX_UNSET, C.INDEX_UNSET,
        )
        val events = Player.Events(
            FlagSet.Builder()
                .addAll(Player.EVENT_MEDIA_METADATA_CHANGED, Player.EVENT_POSITION_DISCONTINUITY)
                .build(),
        )
        listeners.toList().forEach {
            it.onMediaMetadataChanged(mediaMetadata)
            it.onPositionDiscontinuity(position, position, Player.DISCONTINUITY_REASON_INTERNAL)
            it.onEvents(this, events)
        }
    }

    private fun sessionLength(): Long =
        if (currentMediaItem == null) C.TIME_UNSET else MeditationSession.state.value.lengthMs

    private fun elapsed(): Long = MeditationSession.state.value.elapsedMs
}
