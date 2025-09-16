package de.tillhub.printengine.sample.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
expect fun StatusBarAppearance(
    darkTheme: Boolean,
    colorScheme: ColorScheme
)