package br.com.bebidaja.ui.navigation

/** Chaveia o estado transitório do carrinho por usuário ou perfil de demonstração. */
internal fun chaveSessaoCarrinho(usuarioId: String?, perfilDemonstracao: String?): String = when {
    perfilDemonstracao != null -> "demo:$perfilDemonstracao"
    usuarioId != null -> "autenticado:$usuarioId"
    else -> "anonimo"
}

internal fun exigeReautenticacao(uidAnterior: String?, uidNovo: String?, sessaoAutenticada: Boolean): Boolean =
    sessaoAutenticada && uidAnterior != uidNovo
