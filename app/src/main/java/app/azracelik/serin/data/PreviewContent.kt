package app.azracelik.serin.data

/** Compose önizlemeleri için örnek içerik. Önizlemede görseller [bundledImage] üzerinden gösterilir. */
val PreviewContent = SerinContent(
    home = HomeContent(
        banner = "",
        featuredPosts = listOf("history-of-meditation", "power-of-frequencies"),
    ),
    meditations = listOf(
        Meditation("mindfulness", "mindfulness", "Stillness Within", ""),
        Meditation("breathing", "breathing", "Rhythm of Life", ""),
        Meditation("sleep", "sleep", "Nightfall Harmony", ""),
        Meditation("stress-relief", "stress relief", "Inner Refuge", ""),
        Meditation("focus", "focus", "One Point", ""),
        Meditation("chakra", "chakra", "Energy Alignment", ""),
    ),
    blogPosts = listOf(
        BlogPost("history-of-meditation", "The History of Meditation: From Past to Present", ""),
        BlogPost("mindfulness-habit", "21-Day Mindfulness Habit Formation Plan", ""),
        BlogPost("power-of-frequencies", "The Power of Frequencies: 432 Hz and 528 Hz Music", ""),
    ),
)
