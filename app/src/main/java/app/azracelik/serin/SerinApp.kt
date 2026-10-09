package app.azracelik.serin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.azracelik.serin.data.BlogPost
import app.azracelik.serin.data.Meditation
import app.azracelik.serin.playback.PlayerViewModel
import app.azracelik.serin.ui.adaptive.HeightClass
import app.azracelik.serin.ui.adaptive.LocalWindowSize
import app.azracelik.serin.ui.adaptive.WidthClass
import app.azracelik.serin.ui.adaptive.WindowSize
import app.azracelik.serin.ui.blog.BlogPostScreen
import app.azracelik.serin.ui.blog.BlogScreen
import app.azracelik.serin.ui.blog.PostBodyState
import app.azracelik.serin.ui.components.LocalMiniPlayerInset
import app.azracelik.serin.ui.components.MiniPlayer
import app.azracelik.serin.ui.components.MiniPlayerInset
import app.azracelik.serin.ui.components.RailInset
import app.azracelik.serin.ui.components.SerinNavigationRail
import app.azracelik.serin.ui.components.SerinBottomBar
import app.azracelik.serin.ui.components.bottomBarHeight
import app.azracelik.serin.ui.components.SerinTab
import app.azracelik.serin.ui.home.HomeScreen
import app.azracelik.serin.ui.meditation.MeditationDetailScreen
import app.azracelik.serin.ui.meditation.MeditationScreen
import app.azracelik.serin.ui.splash.SplashScreen
import app.azracelik.serin.ui.theme.SerinTheme
import kotlinx.coroutines.delay

private const val SplashFadeOutMillis = 300

private object Routes {
    const val HOME = "home"
    const val MEDITATION = "meditation"
    const val MEDITATION_DETAIL = "meditation/{id}"
    const val BLOG = "blog"
    const val BLOG_POST = "blog/{id}"

    fun meditationDetail(meditation: Meditation) = "meditation/${meditation.id}"
    fun blogPost(post: BlogPost) = "blog/${post.id}"
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
    val route = backStackEntry?.destination?.route
    // Splash, gezinme grafiğinin dışında ana ekranın üstünde çizilir: böylece sekme çubuğu ve geçiş
    // animasyonu splash kapanmadan görünmez.
    var splashVisible by rememberSaveable { mutableStateOf(true) }
    // Sekme çubuğu, splash tamamen solduktan sonra gösterilir.
    var splashGone by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(splashVisible) {
        if (!splashVisible) {
            delay(SplashFadeOutMillis.toLong())
            splashGone = true
        }
    }
    val selectedTab = if (!splashGone) null else when (route) {
        Routes.HOME -> SerinTab.Home
        Routes.MEDITATION, Routes.MEDITATION_DETAIL -> SerinTab.Meditation
        Routes.BLOG, Routes.BLOG_POST -> SerinTab.Blog
        else -> null
    }

    // Seansı süren meditasyon; kendi detay ekranında mini player gösterilmez.
    val sessionMeditation = playback.mediaId?.let(content::meditation)
    val onSessionDetail = route == Routes.MEDITATION_DETAIL &&
        backStackEntry?.arguments?.getString("id") == playback.mediaId
    val showMiniPlayer = selectedTab != null && sessionMeditation != null && !onSessionDetail

    BoxWithConstraints(Modifier.fillMaxSize().background(SerinTheme.colors.background)) {
        val layoutDirection = LocalLayoutDirection.current
        // Yatayda kesim ve sistem çubukları içeriğin dışında kalır; dikeyde ekranlar kendileri halleder.
        val sideInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal)
        val sidePadding = sideInsets.asPaddingValues()
        val widthClass = WidthClass.of(maxWidth)
        val heightClass = HeightClass.of(maxHeight)
        val railInset = if (WindowSize.usesRail(widthClass, heightClass)) RailInset else 0.dp
        val windowSize = WindowSize(
            width = widthClass,
            height = heightClass,
            contentWidth = maxWidth - sidePadding.calculateStartPadding(layoutDirection) -
                sidePadding.calculateEndPadding(layoutDirection) - railInset,
            windowHeight = maxHeight,
        )
        CompositionLocalProvider(LocalWindowSize provides windowSize) {
            Box(Modifier.fillMaxSize().windowInsetsPadding(sideInsets)) {
                // Menü yanda ise içerik onun sağından başlar.
                Box(Modifier.fillMaxSize().padding(start = railInset)) {
                    CompositionLocalProvider(LocalMiniPlayerInset provides if (showMiniPlayer) MiniPlayerInset else 0.dp) {
                        NavHost(navController, startDestination = Routes.HOME) {
                            composable(Routes.HOME) {
                                val lastId by playerViewModel.lastMeditationId.collectAsStateWithLifecycle()
                                HomeScreen(
                                    meditations = content.meditations,
                                    blogPosts = content.blogPosts,
                                    // Seansı süren meditasyonu mini player zaten gösteriyor.
                                    resume = lastId?.takeIf { it != playback.mediaId }?.let(content::meditation),
                                    sessionMinutes = playback.session.lengthMinutes,
                                    onMeditationClick = { navController.navigate(Routes.meditationDetail(it)) },
                                    onStartClick = { meditation ->
                                        val isCurrent = playback.mediaId == meditation.id
                                        if (!(isCurrent && playback.isPlaying)) playerViewModel.togglePlayback(meditation)
                                        navController.navigate(Routes.meditationDetail(meditation))
                                    },
                                    onPostClick = { navController.navigate(Routes.blogPost(it)) },
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
                                        // Başka bir meditasyonun seansı sürüyorsa bu ekran baştan gösterilir.
                                        session = if (isCurrent) playback.session else playback.session.copy(elapsedMs = 0),
                                        onLengthSelect = playerViewModel::setSessionLength,
                                        onPlayClick = { playerViewModel.togglePlayback(meditation) },
                                    )
                                }
                            }
                            composable(Routes.BLOG) {
                                BlogScreen(content.blogPosts, onPostClick = { navController.navigate(Routes.blogPost(it)) })
                            }
                            composable(
                                Routes.BLOG_POST,
                                arguments = listOf(navArgument("id") { type = NavType.StringType }),
                            ) { entry ->
                                content.blogPost(entry.arguments?.getString("id").orEmpty())?.let { post ->
                                    var attempt by remember { mutableIntStateOf(0) }
                                    val body by produceState<PostBodyState>(PostBodyState.Loading, post.body, attempt) {
                                        val url = post.body
                                        value = if (url == null) {
                                            PostBodyState.ComingSoon
                                        } else {
                                            value = PostBodyState.Loading
                                            contentViewModel.loadPost(url)?.let(PostBodyState::Loaded) ?: PostBodyState.Failed
                                        }
                                    }
                                    BlogPostScreen(post, body, onRetry = { attempt++ })
                                }
                            }
                        }
                    }

                    if (showMiniPlayer && sessionMeditation != null) {
                        MiniPlayer(
                            meditation = sessionMeditation,
                            isPlaying = playback.isPlaying,
                            session = playback.session,
                            onClick = { navController.navigate(Routes.meditationDetail(sessionMeditation)) },
                            onToggle = playerViewModel::toggleCurrent,
                            onClose = playerViewModel::stop,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = bottomBarHeight()),
                        )
                    }
                }

                selectedTab?.let { tab ->
                    if (windowSize.useRail) {
                        SerinNavigationRail(
                            selected = tab,
                            onSelect = { navController.navigateToTab(it) },
                            modifier = Modifier.align(Alignment.CenterStart),
                        )
                    } else {
                        SerinBottomBar(
                            selected = tab,
                            onSelect = { navController.navigateToTab(it) },
                            modifier = Modifier.align(Alignment.BottomCenter),
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = splashVisible,
            exit = fadeOut(tween(SplashFadeOutMillis)),
        ) {
            SplashScreen(onFinished = { splashVisible = false })
        }
    }
}

private fun NavHostController.navigateToTab(tab: SerinTab) {
    // Detay ekranları sekmenin kayıtlı durumuna girmesin: aksi halde sekmeye dönünce detay geri yüklenir
    // ve kendi sekmesine dokunmak ekranı değiştirmez. Önce kök ekrana dönülür.
    while (currentDestination?.route.let { it == Routes.MEDITATION_DETAIL || it == Routes.BLOG_POST }) {
        if (!popBackStack()) break
    }
    if (currentDestination?.route == tab.route) return
    navigate(tab.route) {
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
