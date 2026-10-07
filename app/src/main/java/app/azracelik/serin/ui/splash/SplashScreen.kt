package app.azracelik.serin.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.azracelik.serin.R
import app.azracelik.serin.ui.components.SerinHeader
import app.azracelik.serin.ui.theme.SerinTheme
import app.azracelik.serin.ui.theme.SerinType
import kotlinx.coroutines.delay

private const val SplashDurationMillis = 2000L

@Composable
fun SplashScreen(onFinished: () -> Unit, modifier: Modifier = Modifier) {
    val currentOnFinished by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        delay(SplashDurationMillis)
        currentOnFinished()
    }

    Box(modifier.fillMaxSize().background(Color.White)) {
        SerinHeader()
        Text(
            text = stringResource(R.string.splash_slogan),
            style = SerinType.Splash,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-32).dp)
                .width(226.dp),
        )
        Image(
            painter = painterResource(R.drawable.splash_logo),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 17.dp)
                .size(100.dp),
        )
    }
}

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun SplashScreenPreview() {
    SerinTheme { SplashScreen(onFinished = {}) }
}
