package app.azracelik.serin.data

/** Compose önizlemeleri için örnek içerik. Önizlemede görseller [bundledImage] üzerinden gösterilir. */
val PreviewContent = SerinContent(
    home = HomeContent(
        banner = "",
        featuredPosts = listOf("history-of-meditation", "power-of-frequencies"),
    ),
    meditations = listOf(
        Meditation("mindfulness", "farkındalık", "İçsel Dinginlik", ""),
        Meditation("breathing", "nefes", "Hayatın Ritmi", ""),
        Meditation("sleep", "uyku", "Akşamın Uyumu", ""),
        Meditation("stress-relief", "stres azaltma", "İç Sığınak", ""),
        Meditation("focus", "odak", "Tek Nokta", ""),
        Meditation("chakra", "çakra", "Enerji Dengesi", ""),
    ),
    blogPosts = listOf(
        BlogPost("history-of-meditation", "Meditasyonun Tarihi: Geçmişten Bugüne", ""),
        BlogPost("mindfulness-habit", "21 Günde Farkındalık Alışkanlığı Planı", ""),
        BlogPost("power-of-frequencies", "Frekansların Gücü: 432 Hz ve 528 Hz Müzik", ""),
    ),
)
