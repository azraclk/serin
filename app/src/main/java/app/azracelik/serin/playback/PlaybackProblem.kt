package app.azracelik.serin.playback

/** Sesin neden çalmadığı; kullanıcıya ne yapabileceği söylensin diye ikiye ayrılır. */
enum class PlaybackProblem {
    /** Ses dosyasına ulaşılamadı: genellikle internet yok. İndirilmiş kopya bunu çözer. */
    LoadFailed,

    /** Ses açılamadı ya da çözülemedi. */
    Other,
}

/** ExoPlayer'ın `PlaybackException.errorCode` değerleri: 2xxx dosya ya da ağ okuma hatalarıdır. */
fun playbackProblemOf(errorCode: Int): PlaybackProblem =
    if (errorCode / 1000 == 2) PlaybackProblem.LoadFailed else PlaybackProblem.Other
