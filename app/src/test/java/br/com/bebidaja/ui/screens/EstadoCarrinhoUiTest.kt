package br.com.bebidaja.ui.screens

import br.com.bebidaja.data.EnderecoEntrega
import br.com.bebidaja.data.FormaPagamento
import br.com.bebidaja.data.ProdutoCarrinho
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EstadoCarrinhoUiTest {
    @Test
    fun limparSessaoRemoveCarrinhoEnderecoPagamentoERenovaChaveDeConfirmacao() {
        val estado = EstadoCarrinhoUi()
        estado.adicionar(ProdutoCarrinho("produto-1", "empresa-1", "Agua", 500))
        estado.salvarEndereco(EnderecoEntrega("Rua A", "10", "Centro", "Goiana", "55900000"))
        estado.selecionarFormaPagamento(FormaPagamento.PIX)
        val chaveAnterior = estado.chaveConfirmacao

        estado.limparSessao()

        assertTrue(estado.carrinho.estaVazio)
        assertNull(estado.endereco)
        assertNull(estado.formaPagamento)
        assertTrue(estado.chaveConfirmacao != chaveAnterior)
    }
}
