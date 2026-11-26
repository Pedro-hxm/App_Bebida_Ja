package br.com.bebidaja.ui.navigation

import br.com.bebidaja.data.TipoPerfil

/** Dados fictícios usados somente durante a demonstração local. */
data class DemoSession(
    val nome: String,
    val email: String,
    val tipoPerfil: TipoPerfil
)

fun criarDemoSession(tipoPerfil: TipoPerfil): DemoSession = when (tipoPerfil) {
    TipoPerfil.CLIENTE -> DemoSession("Cliente Demonstração", "cliente@demonstracao.com", tipoPerfil)
    TipoPerfil.EMPRESA -> DemoSession("Empresa Demonstração", "empresa@demonstracao.com", tipoPerfil)
}
