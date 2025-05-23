package ru.batir8888.beautymanager.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private val PurpleDeep   = Color(0xFF7B1FA2)   // #7B1FA2
private val PurpleLight  = Color(0xFFE9D7FF)   // очень светлый

private val LightColors = lightColorScheme(
    primary        = PurpleDeep,
    secondary      = Color(0xFF9C27B0),
    onPrimary      = PurpleLight,
    onBackground   = Color(0xFF2A1546),
    background     = Color.Transparent,
    surface        = Color.Transparent,
    onSurface      = Color(0xFF2A1546),
)

private val DarkTop    = Color(0xFF11162A)
private val DarkBottom = Color(0xFF121538)

private val DarkColors = darkColorScheme(
    primary      = Color(0xFF5B67D0),
    secondary    = Color(0xFF363FD7),
    onPrimary    = DarkBottom,
    onBackground = Color(0xFF422475),
    background   = Color.Transparent,
    surface      = Color.Transparent,
    onSurface    = Color(0xFF3E246B),
)

@Composable
fun BeautyManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors  = if (darkTheme) DarkColors else LightColors

    val gradient = if (darkTheme) {
        Brush.verticalGradient(listOf(DarkTop, DarkBottom))
    } else {
        Brush.verticalGradient(listOf(PurpleDeep, PurpleLight))
    }

    MaterialTheme(colorScheme = colors, typography = Typography) {
        Box(
            Modifier
                .fillMaxSize()
                .background(gradient)
        ) {
            Surface(color = Color.Transparent) { content() }
        }
    }
}