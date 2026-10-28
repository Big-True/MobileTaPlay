package com.mobiletaplay.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val MtpBg = Color(0xFF0F1419)
val MtpPanel = Color(0xFF1A222C)
val MtpInk = Color(0xFFE7ECF1)
val MtpMuted = Color(0xFF8B9BAB)
val MtpAccent = Color(0xFF3D8BFD)

private val Scheme = darkColorScheme(
    primary = MtpAccent,
    onPrimary = Color.White,
    background = MtpBg,
    onBackground = MtpInk,
    surface = MtpPanel,
    onSurface = MtpInk,
    surfaceVariant = MtpPanel,
    onSurfaceVariant = MtpMuted,
)

@Composable
fun MobileTaPlayTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}
