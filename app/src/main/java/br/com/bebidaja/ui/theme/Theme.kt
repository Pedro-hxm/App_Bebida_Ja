package br.com.bebidaja.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CoresBebidaJa = lightColorScheme(
    primary = AzulMedio,
    onPrimary = Branco,
    secondary = AzulClaro,
    onSecondary = AzulEscuro,
    background = Branco,
    surface = Branco,
    onSurface = AzulEscuro,
    surfaceVariant = AzulClaro,
    onSurfaceVariant = AzulEscuro
)

@Composable
fun BebidaJaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CoresBebidaJa,
        content = content
    )
}