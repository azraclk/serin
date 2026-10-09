package app.azracelik.serin

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.azracelik.serin.playback.MeditationSession
import app.azracelik.serin.playback.SessionTimer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Seans zamanlayıcısı gerçek bir ExoPlayer ile: ses yavaşça açılır, süre işler, süre dolunca seans biter. */
@RunWith(AndroidJUnit4::class)
class SessionTimerTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    private lateinit var player: ExoPlayer
    private lateinit var timer: SessionTimer
    private lateinit var audio: File

    /** Ağa bağlı kalmamak için 3 saniyelik sessiz bir WAV dosyası üretilir. */
    private fun silentWav(): File {
        val sampleRate = 8000
        val data = ByteArray(sampleRate * 2 * 3)
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray()); putInt(36 + data.size); put("WAVE".toByteArray())
            put("fmt ".toByteArray()); putInt(16); putShort(1); putShort(1)
            putInt(sampleRate); putInt(sampleRate * 2); putShort(2); putShort(16)
            put("data".toByteArray()); putInt(data.size)
        }
        return File(instrumentation.targetContext.cacheDir, "silence.wav").apply {
            outputStream().use { it.write(header.array()); it.write(data) }
        }
    }

    private fun onMain(block: () -> Unit) = instrumentation.runOnMainSync(block)

    private fun <T> readMain(block: () -> T): T {
        var result: T? = null
        instrumentation.runOnMainSync { result = block() }
        @Suppress("UNCHECKED_CAST")
        return result as T
    }

    @Before
    fun setUp() {
        audio = silentWav()
        onMain {
            player = ExoPlayer.Builder(instrumentation.targetContext).build()
            timer = SessionTimer(player, scope).also { it.start() }
        }
    }

    @After
    fun tearDown() {
        onMain {
            timer.release()
            player.release()
            MeditationSession.setLength(MeditationSession.DefaultLengthMinutes)
        }
        scope.cancel()
        audio.delete()
    }

    private fun startPlaying() {
        onMain {
            player.setMediaItem(MediaItem.fromUri(Uri.fromFile(audio)))
            player.prepare()
            player.play()
        }
        val deadline = System.currentTimeMillis() + 10_000
        while (!readMain { player.isPlaying }) {
            assertTrue("Ses çalmaya başlamadı", System.currentTimeMillis() < deadline)
            Thread.sleep(50)
        }
    }

    @Test
    fun loopsTheTrackForTheWholeSession() {
        assertEquals(Player.REPEAT_MODE_ONE, readMain { player.repeatMode })
    }

    @Test
    fun sessionStartsSilentAndFadesInWhileTimeAdvances() {
        onMain { MeditationSession.setLength(10) }
        startPlaying()
        Thread.sleep(2_300)
        val volume = readMain { player.volume }
        val elapsed = MeditationSession.state.value.elapsedMs
        assertTrue("Ses açılmaya başlamalı, şimdi $volume", volume > 0f)
        assertTrue("Ses 3 sn dolmadan tam açılmamalı, şimdi $volume", volume < 1f)
        assertTrue("Süre ilerlemeli, şimdi $elapsed ms", elapsed in 1_000..3_000)
        assertEquals("Süre saniyeye yuvarlanmalı", 0L, elapsed % 1000)
    }

    @Test
    fun pausingStopsTheClock() {
        onMain { MeditationSession.setLength(10) }
        startPlaying()
        Thread.sleep(1_500)
        onMain { player.pause() }
        Thread.sleep(300)
        val frozen = MeditationSession.state.value.elapsedMs
        Thread.sleep(1_500)
        assertEquals(frozen, MeditationSession.state.value.elapsedMs)
    }

    @Test
    fun sessionEndsWhenLengthIsReached() {
        // Seans süreleri dakikayla seçilir; 0 dk, ilk tıkta süresi dolmuş bir seans demektir.
        onMain { MeditationSession.setLength(0) }
        startPlaying()
        val deadline = System.currentTimeMillis() + 5_000
        while (readMain { player.playWhenReady }) {
            assertTrue("Süre dolunca ses duraklatılmalı", System.currentTimeMillis() < deadline)
            Thread.sleep(50)
        }
        assertFalse(readMain { player.isPlaying })
        assertEquals("Ses başa sarılmalı", 0L, readMain { player.currentPosition })
        assertEquals("Seans sıfırlanmalı", 0L, MeditationSession.state.value.elapsedMs)
    }
}
