package ru.batir8888.beautymanager.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Основные цвета бренда для beauty индустрии
private val RoseGold = Color(0xFFE8B4B8)       // Розовое золото
private val DeepRose = Color(0xFFD946A0)       // Глубокий розовый
private val SoftPink = Color(0xFFFDF2F8)       // Мягкий розовый
private val WarmWhite = Color(0xFFFFFBFE)      // Теплый белый
private val CharcoalGray = Color(0xFF374151)   // Угольно-серый
private val SoftGray = Color(0xFFF9FAFB)       // Мягкий серый

// Акцентные цвета
private val GoldenAccent = Color(0xFFF59E0B)   // Золотой акцент
private val SuccessGreen = Color(0xFF10B981)   // Зеленый успеха
private val ErrorRed = Color(0xFFEF4444)       // Красный ошибки

// Светлая тема - элегантная и женственная
private val LightColors = lightColorScheme(
    primary = DeepRose,
    onPrimary = WarmWhite,
    primaryContainer = SoftPink,
    onPrimaryContainer = CharcoalGray,

    secondary = GoldenAccent,
    onSecondary = WarmWhite,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),

    tertiary = Color(0xFF8B5CF6),
    onTertiary = WarmWhite,

    background = Color.Transparent,
    onBackground = CharcoalGray,

    surface = Color(0xFFFFFBFE).copy(alpha = 0.95f),
    onSurface = CharcoalGray,
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = Color(0xFF6B7280),

    outline = Color(0xFFD1D5DB),
    outlineVariant = Color(0xFFE5E7EB),

    error = ErrorRed,
    onError = WarmWhite,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),
)

// Темная тема - роскошная и премиальная
private val DarkGradientTop = Color(0xFF1F1B2E)    // Темно-фиолетовый
private val DarkGradientBottom = Color(0xFF2A1F3D) // Глубокий баклажан

private val DarkColors = darkColorScheme(
    primary = RoseGold,
    onPrimary = Color(0xFF1F1B2E),
    primaryContainer = Color(0xFF4A1D4A),
    onPrimaryContainer = RoseGold,

    secondary = GoldenAccent,
    onSecondary = Color(0xFF1F1B2E),
    secondaryContainer = Color(0xFF92400E),
    onSecondaryContainer = Color(0xFFFEF3C7),

    tertiary = Color(0xFFC084FC),
    onTertiary = Color(0xFF1F1B2E),

    background = Color.Transparent,
    onBackground = Color(0xFFF8FAFC),

    surface = Color(0xFF2D1B3D).copy(alpha = 0.95f),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF3D2A4A),
    onSurfaceVariant = Color(0xFFD1D5DB),

    outline = Color(0xFF6B7280),
    outlineVariant = Color(0xFF4B5563),

    error = Color(0xFFF87171),
    onError = Color(0xFF1F1B2E),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
)

@Composable
fun BeautyManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    val gradient = if (darkTheme) {
        Brush.verticalGradient(
            colors = listOf(
                DarkGradientTop,
                DarkGradientBottom,
                Color(0xFF1A0B2E)
            ),
            startY = 0f,
            endY = Float.POSITIVE_INFINITY
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                SoftPink,
                WarmWhite,
                Color(0xFFFDF2F8)
            ),
            startY = 0f,
            endY = Float.POSITIVE_INFINITY
        )
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = Shapes()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
        ) {
            Surface(
                color = Color.Transparent,
                modifier = Modifier.fillMaxSize()
            ) {
                content()
            }
        }
    }
}