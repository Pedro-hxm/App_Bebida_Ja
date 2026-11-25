package br.com.bebidaja.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CarrinhoTest {
    private val agua = ProdutoCarrinho(
        produtoId = "agua-1",
        empresaId = "empresa-1",
        nome = "Água mineral",
        precoUnitarioCentavos = 450
    )

    @Test
    fun adicionarMesmoProdutoSomaQuantidadeESubtotal() {
        val carrinho = Carrinho()

        carrinho.adicionar(agua)
        carrinho.adicionar(agua, quantidade = 2)

        assertEquals(1, carrinho.itens.size)
        assertEquals(3, carrinho.itens.single().quantidade)
        assertEquals(1_350L, carrinho.subtotalCentavos)
        assertEquals(carrinho.subtotalCentavos, carrinho.totalCentavos)
    }

    @Test
    fun alterarQuantidadeAtualizaTotal() {
        val carrinho = Carrinho()
        carrinho.adicionar(agua)

        carrinho.alterarQuantidade("agua-1", "empresa-1", 4)

        assertEquals(1_800L, carrinho.totalCentavos)
    }

    @Test
    fun removerUltimoItemDeixaCarrinhoVazio() {
        val carrinho = Carrinho()
        carrinho.adicionar(agua)

        carrinho.remover("agua-1", "empresa-1")

        assertTrue(carrinho.estaVazio)
        assertEquals(0L, carrinho.totalCentavos)
    }

    @Test
    fun carrinhoInicialComecaVazio() {
        val carrinho = Carrinho()

        assertTrue(carrinho.estaVazio)
        assertFalse(carrinho.itens.isNotEmpty())
        assertEquals(0L, carrinho.subtotalCentavos)
    }

    @Test(expected = IllegalArgumentException::class)
    fun naoAceitaQuantidadeInvalidaAoAdicionar() {
        Carrinho().adicionar(agua, quantidade = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun naoAceitaPrecoNegativo() {
        ProdutoCarrinho("produto", "empresa", "Bebida", -1)
    }
}
