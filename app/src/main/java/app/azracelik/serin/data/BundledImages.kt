package app.azracelik.serin.data

import androidx.annotation.DrawableRes
import app.azracelik.serin.R

const val HomeBannerId = "home-banner"

/**
 * Uygulamayla gelen görseller. İnternet yokken ve görsel henüz indirilmemişken bunlar gösterilir.
 * Sonradan eklenen içeriklerin karşılığı yoktur; onlar indirilene kadar boş kalır.
 */
@DrawableRes
fun bundledImage(id: String): Int? = when (id) {
    HomeBannerId -> R.drawable.home_banner
    "mindfulness" -> R.drawable.med_mindfulness
    "breathing" -> R.drawable.med_breathing
    "sleep" -> R.drawable.med_sleep
    "stress-relief" -> R.drawable.med_stress_relief
    "focus" -> R.drawable.med_focus
    "chakra" -> R.drawable.med_chakra
    "history-of-meditation" -> R.drawable.blog_inner_refuge
    "mindfulness-habit" -> R.drawable.blog_stillness_within
    "power-of-frequencies" -> R.drawable.blog_energy_alignment
    else -> null
}
