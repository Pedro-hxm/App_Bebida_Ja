package br.com.bebidaja.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CoresBebidaJa = lightColorScheme(
    primary = AzulMedio,
    onPrimary = Branco,
    primaryContainer = AzulClaro,
    onPrimaryContainer = AzulEscuro,
    secondary = AzulMedio,
    onSecondary = Branco,
    secondaryContainer = AzulClaro,
    onSecondaryContainer = AzulEscuro,
    background = Branco,
    onBackground = AzulEscuro,
    surface = Branco,
    onSurface = AzulEscuro,
    surfaceVariant = AzulClaro,
    onSurfaceVariant = AzulEscuro,
    surfaceContainerLowest = Branco,
    surfaceContainerLow = Branco,
    surfaceContainer = AzulClaro,
    surfaceContainerHigh = AzulClaro,
    surfaceContainerHighest = AzulClaro
)

@Composable
fun BebidaJaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CoresBebidaJa,
        typography = TipografiaBebidaJa,
        content = content
    )
}