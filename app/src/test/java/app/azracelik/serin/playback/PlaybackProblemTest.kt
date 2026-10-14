package app.azracelik.serin.playback

import androidx.media3.common.PlaybackException
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackProblemTest {
    @Test
    fun networkAndFileErrorsAreLoadFailures() {
        listOf(
            PlaybackException.ERROR_CODE_IO_UNSPECIFIED,
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
            PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS,
            PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND,
        ).forEach { assertEquals(PlaybackProblem.LoadFailed, playbackProblemOf(it)) }
    }

    @Test
    fun otherErrorsAreGeneric() {
        listOf(
            PlaybackException.ERROR_CODE_UNSPECIFIED,
            PlaybackException.ERROR_CODE_DECODING_FAILED,
            PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
        ).forEach { assertEquals(PlaybackProblem.Other, playbackProblemOf(it)) }
    }
}
