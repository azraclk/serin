package app.azracelik.serin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.azracelik.serin.data.Meditation
import app.azracelik.serin.playback.PlayerViewModel
import app.azracelik.serin.ui.blog.BlogScreen
import app.azracelik.serin.ui.components.SerinBottomBar
import app.azracelik.serin.ui.components.SerinTab
import app.azracelik.serin.ui.home.HomeScreen
import app.azracelik.serin.ui.meditation.MeditationDetailScreen
import app.azracelik.serin.ui.meditation.MeditationScreen
import app.azracelik.serin.ui.splash.SplashScreen

private object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val MEDITATION = "meditation"
    const val MEDITATION_DETAIL = "meditation/{id}"
    const val BLOG = "blog"

    fun meditationDetail(meditation: Meditation) = "meditation/${meditation.id}"
}

private val SerinTab.route
    get() = when (this) {
        SerinTab.Home -> Routes.HOME
        SerinTab.Meditation -> Routes.MEDITATION
        SerinTab.Blog -> Routes.BLOG
    }

@Composable
fun SerinApp(
    contentViewModel: ContentViewModel = viewModel(),
    playerViewModel: PlayerViewModel = viewModel(),
) {
    val content by contentViewModel.content.collectAsStateWithLifecycle()
    val playback by playerViewModel.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val selectedTab = when (backStackEntry?.destination?.route) {
        Routes.HOME -> SerinTab.Home
        Routes.MEDITATION, Routes.MEDITATION_DETAIL -> SerinTab.Meditation
        Routes.BLOG -> SerinTab.Blog
        else -> null
    }

    Box(Modifier.fillMaxSize().background(Color.White)) {
        NavHost(navController, startDestination = Routes.SPLASH) {
            composable(Routes.SPLASH) {
                SplashScreen(onFinished = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                })
            }
            composable(Routes.HOME) {
                HomeScreen(
                    bannerUrl = content.home.banner,
                    featuredPosts = content.featuredPosts,
                    onBannerClick = { navController.navigateToTab(SerinTab.Meditation) },
                    onPostClick = { navController.navigateToTab(SerinTab.Blog) },
                )
            }
            composable(Routes.MEDITATION) {
                MeditationScreen(content.meditations, onMeditationClick = { navController.navigate(Routes.meditationDetail(it)) })
            }
            composable(
                Routes.MEDITATION_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                // İçerik güncellenip bu meditasyon kaldırılmışsa ekran boş kalır.
                content.meditation(entry.arguments?.getString("id").orEmpty())?.let { meditation ->
                    val isCurrent = playback.mediaId == meditation.id
                    MeditationDetailScreen(
                        meditation = meditation,
                        isPlaying = isCurrent && playback.isPlaying,
                        progress = if (isCurrent) playback.progress else 0f,
                        onPlayClick = { playerViewModel.togglePlayback(meditation) },
                    )
                }
            }
            composable(Routes.BLOG) {
                // Blog yazısı detay ekranı henüz tasarımda yok.
                BlogScreen(content.blogPosts, onPostClick = {})
            }
        }

        selectedTab?.let { tab ->
            SerinBottomBar(
                selected = tab,
                onSelect = { navController.navigateToTab(it) },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

private fun NavHostController.navigateToTab(tab: SerinTab) {
    navigate(tab.route) {
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
