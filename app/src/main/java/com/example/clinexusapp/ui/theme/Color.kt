package com.example.clinexusapp.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Vibrant Teal Neumorphic Palette
val DeepTeal = Color(0xFF00A896)
val VibrantTeal = Color(0xFF00C9B1)
val MintSparkle = Color(0xFFE0F7F4)
val SoftMist = Color(0xFFF2F9F8)
val RoyalNavy = Color(0xFF2C3E50)
val PureWhite = Color(0xFFFFFFFF)

// Additional UI Tones
val TealMuted = Color(0xFF7FC8C0)
val SlateGray = Color(0xFF64748B)
val LightSlate = Color(0xFF94A3B8)
val ErrorRed = Color(0xFFEF4444)
val DarkRed = Color(0xFF8B0000)

// Standard Colors (mapped for compatibility)
val BluePrimary = DeepTeal
val BlueSecondary = VibrantTeal
val BlueDark = RoyalNavy
val White = PureWhite
val Black = RoyalNavy
val GrayDark = SlateGray
val GrayMedium = MintSparkle

// Premium Gradients
val WavyTealGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF00D2FF), // Luminous Cyan
        VibrantTeal,       // Mid Teal
        DeepTeal           // Deep Teal
    )
)

val ActionButtonGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF4FC3F7), // Soft Cerulean
        VibrantTeal        // Deep Teal
    )
)

// Wave Translucency
val WaveLayerTop = Color(0xFFFFFFFF).copy(alpha = 0.15f)
val WaveLayerMid = Color(0xFFE0F7F4).copy(alpha = 0.25f)
val WaveLayerBase = VibrantTeal.copy(alpha = 0.35f)

val SoftMintGradient = Brush.verticalGradient(
    colors = listOf(PureWhite, SoftMist)
)

val PremiumBlueGradient = WavyTealGradient
val BlueExtraLight = MintSparkle
val LuminousAzureGradient = WavyTealGradient
