package br.com.bebidaja.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SessaoCarrinhoTest {
    @Test
    fun trocaEntreContasAutenticadasGeraEstadosSeparados() {
        assertNotEquals(chaveSessaoCarrinho("cliente-1", null), chaveSessaoCarrinho("cliente-2", null))
    }

    @Test
    fun contaAutenticadaNaoCompartilhaCarrinhoComDemonstracao() {
        assertNotEquals(chaveSessaoCarrinho("cliente-1", null), chaveSessaoCarrinho(null, "cliente"))
    }

    @Test
    fun perfisDeDemonstracaoTemCarrinhosSeparados() {
        assertNotEquals(chaveSessaoCarrinho(null, "cliente"), chaveSessaoCarrinho(null, "empresa"))
    }

    @Test
    fun sessoesAnonimasUsamChaveEstavel() {
        assertEquals("anonimo", chaveSessaoCarrinho(null, null))
    }

    @Test
    fun uidAlteradoDuranteSessaoAutenticadaExigeNovaAutenticacao() {
        assertEquals(true, exigeReautenticacao("cliente-1", "cliente-2", true))
        assertEquals(true, exigeReautenticacao("cliente-1", null, true))
    }

    @Test
    fun loginInicialEUidInalteradoNaoInvalidamSessao() {
        assertEquals(false, exigeReautenticacao(null, "cliente-1", false))
        assertEquals(false, exigeReautenticacao("cliente-1", "cliente-1", true))
    }
}
