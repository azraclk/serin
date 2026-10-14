package app.azracelik.serin.data

import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

class AudioStoreTest {
    private lateinit var dir: File
    private var requests = 0
    private var code = 200
    private val audio = ByteArray(1000) { it.toByte() }

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            requests++
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message("")
                .body(audio.toResponseBody("audio/wav".toMediaType()))
                .build()
        }
        .build()

    @Before
    fun setUp() {
        dir = Files.createTempDirectory("audio").toFile()
    }

    @After
    fun tearDown() {
        dir.deleteRecursively()
    }

    private val url = "https://example.com/sleep.wav"
    private val key = AudioStore.key("sleep", url)

    @Test
    fun downloadSavesFileAndMarksDownloaded() = runTest {
        val store = AudioStore(File(dir, "audio"), client)
        assertNull(store.localFile("sleep", url))

        store.download("sleep", url)

        assertEquals(AudioStatus.Downloaded, store.status.value[key])
        assertArrayEquals(audio, store.localFile("sleep", url)!!.readBytes())
        assertEquals(listOf(key), File(dir, "audio").list()!!.toList())
    }

    @Test
    fun failedDownloadLeavesNoFileAndCanBeRetried() = runTest {
        val store = AudioStore(File(dir, "audio"), client)
        code = 404
        store.download("sleep", url)
        assertEquals(AudioStatus.Failed, store.status.value[key])
        assertNull(store.localFile("sleep", url))
        assertEquals(emptyList<String>(), File(dir, "audio").list()!!.toList())

        code = 200
        store.download("sleep", url)
        assertEquals(AudioStatus.Downloaded, store.status.value[key])
    }

    @Test
    fun downloadedAudioIsNotFetchedAgain() = runTest {
        val store = AudioStore(File(dir, "audio"), client)
        store.download("sleep", url)
        store.download("sleep", url)
        assertEquals(1, requests)
    }

    @Test
    fun removeDeletesFileAndStatus() = runTest {
        val store = AudioStore(File(dir, "audio"), client)
        store.download("sleep", url)
        store.remove("sleep", url)
        assertNull(store.status.value[key])
        assertNull(store.localFile("sleep", url))
        assertEquals(emptyList<String>(), File(dir, "audio").list()!!.toList())
    }

    @Test
    fun downloadsSurviveRestartAndPartialFilesAreDropped() = runTest {
        AudioStore(File(dir, "audio"), client).download("sleep", url)
        File(dir, "audio/focus@1.part").writeText("yarım")

        val reopened = AudioStore(File(dir, "audio"), client)

        assertEquals(mapOf(key to AudioStatus.Downloaded), reopened.status.value)
        assertEquals(listOf(key), File(dir, "audio").list()!!.toList())
    }

    @Test
    fun newAddressReplacesTheOldCopyOfTheSameAudio() = runTest {
        val store = AudioStore(File(dir, "audio"), client)
        store.download("sleep", url)
        val newUrl = "https://example.com/sleep-v2.wav"
        store.download("sleep", newUrl)

        assertEquals(listOf(AudioStore.key("sleep", newUrl)), File(dir, "audio").list()!!.toList())
    }

    @Test
    fun audioWithAPrefixedIdIsNotTouched() = runTest {
        val store = AudioStore(File(dir, "audio"), client)
        val other = "https://example.com/stress-relief.wav"
        store.download("stress-relief", other)
        store.download("stress", "https://example.com/stress.wav")

        assertNotEquals(null, store.localFile("stress-relief", other))
    }
}
