package app.azracelik.serin.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import app.azracelik.serin.R

enum class Meditation(
    @StringRes val label: Int,
    @StringRes val title: Int,
    @DrawableRes val image: Int,
) {
    Mindfulness(R.string.med_mindfulness, R.string.med_title_mindfulness, R.drawable.med_mindfulness),
    Breathing(R.string.med_breathing, R.string.med_title_breathing, R.drawable.med_breathing),
    Sleep(R.string.med_sleep, R.string.med_title_sleep, R.drawable.med_sleep),
    StressRelief(R.string.med_stress_relief, R.string.med_title_stress_relief, R.drawable.med_stress_relief),
    Focus(R.string.med_focus, R.string.med_title_focus, R.drawable.med_focus),
    Chakra(R.string.med_chakra, R.string.med_title_chakra, R.drawable.med_chakra),
}

enum class BlogPost(
    @StringRes val title: Int,
    @DrawableRes val image: Int,
) {
    History(R.string.blog_history, R.drawable.blog_inner_refuge),
    Habit(R.string.blog_habit, R.drawable.blog_stillness_within),
    Frequencies(R.string.blog_frequencies, R.drawable.blog_energy_alignment),
}
