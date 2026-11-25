package br.com.bebidaja.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PedidosTest {
    private fun criarCarrinho(): Carrinho =
        Carrinho().apply {
            adicionar(
                ProdutoCarrinho(
                    produtoId = "bebida-1",
                    empresaId = "empresa-demo",
                    nome = "Água mineral",
                    precoUnitarioCentavos = 450
                ),
                quantidade = 2
            )
        }

    private val enderecoValido = EnderecoEntrega(
        rua = "Rua das Flores",
        numero = "10",
        bairro = "Centro",
        cidade = "Goiana",
        cep = "55900000"
    )

    @Test
    fun criaPedidoComDadosDoCarrinhoEStatusRecebido() {
        val carrinho = criarCarrinho()
        val repositorio = RepositorioPedidosEmMemoria()

        val resultado = repositorio.confirmarPedido(
            clienteId = "cliente-teste",
            carrinho = carrinho,
            endereco = enderecoValido,
            formaPagamento = FormaPagamento.PIX,
            referenciaComprovante = "DEMO-BEBIDAJA-001",
            chaveConfirmacao = "checkout-teste",
            criadoEmMillis = 100L
        )

        val pedido = resultado.getOrThrow()
        assertEquals("cliente-teste", pedido.clienteId)
        assertEquals("empresa-demo", pedido.empresaId)
        assertEquals(2, pedido.itens.single().quantidade)
        assertEquals(900L, pedido.subtotalCentavos)
        assertEquals(900L, pedido.totalCentavos)
        assertEquals(StatusPedido.RECEBIDO, pedido.status)
        assertEquals(100L, pedido.criadoEmMillis)
    }

    @Test
    fun confirmacaoRepetidaComMesmaChaveNaoDuplicaPedido() {
        val repositorio = RepositorioPedidosEmMemoria()
        val carrinho = criarCarrinho()

        val primeiro = repositorio.confirmarPedido(
            "cliente-teste", carrinho, enderecoValido, FormaPagamento.DINHEIRO,
            "DEMO-BEBIDAJA-001", "mesma-chave"
        ).getOrThrow()
        val segundo = repositorio.confirmarPedido(
            "cliente-teste", carrinho, enderecoValido, FormaPagamento.DINHEIRO,
            "DEMO-BEBIDAJA-001", "mesma-chave"
        ).getOrThrow()

        assertEquals(primeiro.id, segundo.id)
        assertEquals(1, repositorio.listarPorCliente("cliente-teste").size)
    }

    @Test
    fun carrinhoDeEmpresasDiferentesNaoCriaPedido() {
        val carrinho = criarCarrinho().apply {
            adicionar(ProdutoCarrinho("bebida-2", "outra-empresa", "Suco", 700))
        }
        val repositorio = RepositorioPedidosEmMemoria()

        val resultado = repositorio.confirmarPedido(
            "cliente-teste", carrinho, enderecoValido, FormaPagamento.PIX,
            "DEMO-BEBIDAJA-001", "multiempresa"
        )

        assertTrue(resultado.isFailure)
        assertTrue(repositorio.listarPorCliente("cliente-teste").isEmpty())
    }
}
