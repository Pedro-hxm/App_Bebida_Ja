package br.com.bebidaja.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight

private val tipografiaPadrao = Typography()

val TipografiaBebidaJa = Typography(
    headlineMedium = tipografiaPadrao.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
    headlineSmall = tipografiaPadrao.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = tipografiaPadrao.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = tipografiaPadrao.titleMedium.copy(fontWeight = FontWeight.Medium)
)
