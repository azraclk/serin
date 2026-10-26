package app.azracelik.serin.playback

import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player

/**
 * Bildirimin ve kilit ekranının gördüğü oynatıcı. Ses döngüde çalındığı için kendi süresi ve konumu seansı
 * anlatmaz; burada seçilen seans süresi ve geçen süre bildirilir, böylece ilerleme çubuğu seansla birlikte
 * dolar. Seans içinde atlamanın anlamı olmadığından ileri/geri sarma komutları kapalıdır.
 */
internal class SessionPlayer(player: Player) : ForwardingPlayer(player) {
    private val seekCommands = intArrayOf(
        Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM,
        Player.COMMAND_SEEK_TO_PREVIOUS,
        Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
        Player.COMMAND_SEEK_TO_NEXT,
        Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
    )

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

    private fun sessionLength(): Long =
        if (currentMediaItem == null) C.TIME_UNSET else MeditationSession.state.value.lengthMs

    private fun elapsed(): Long = MeditationSession.state.value.elapsedMs
}
