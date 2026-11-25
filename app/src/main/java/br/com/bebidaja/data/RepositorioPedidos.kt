package br.com.bebidaja.data

interface RepositorioPedidos {
    fun buscarPorChaveConfirmacao(chave: String): Pedido?
    fun salvar(pedido: Pedido): Pedido
    fun listarPorCliente(clienteId: String): List<Pedido>
}

/**
 * Persistência temporária em memória para a demonstração.
 * Deve ser substituída pelo contrato definido com Guilherme antes da integração final.
 */
class RepositorioPedidosEmMemoria : RepositorioPedidos {
    private val pedidosPorChave = linkedMapOf<String, Pedido>()

    override fun buscarPorChaveConfirmacao(chave: String): Pedido? =
        pedidosPorChave[chave]

    override fun salvar(pedido: Pedido): Pedido {
        return pedidosPorChave.getOrPut(pedido.chaveConfirmacao) { pedido }
    }

    override fun listarPorCliente(clienteId: String): List<Pedido> =
        pedidosPorChave.values
            .filter { it.clienteId == clienteId }
            .sortedByDescending { it.criadoEmMillis }

    fun confirmarPedido(
        clienteId: String,
        carrinho: Carrinho,
        endereco: EnderecoEntrega?,
        formaPagamento: FormaPagamento?,
        referenciaComprovante: String,
        chaveConfirmacao: String,
        criadoEmMillis: Long = System.currentTimeMillis()
    ): Result<Pedido> {
        if (chaveConfirmacao.isBlank()) return Result.failure(IllegalArgumentException("Confirmação inválida."))
        buscarPorChaveConfirmacao(chaveConfirmacao)?.let { return Result.success(it) }
        if (clienteId.isBlank() || carrinho.estaVazio || endereco?.estaValido != true || formaPagamento == null) {
            return Result.failure(IllegalArgumentException("Dados do pedido inválidos."))
        }
        if (!referenciaComprovante.startsWith("DEMO-")) {
            return Result.failure(IllegalArgumentException("O comprovante deve ser demonstrativo."))
        }
        val itensCarrinho = carrinho.itens
        val empresas = itensCarrinho.map { it.produto.empresaId }.distinct()
        if (empresas.size != 1) {
            return Result.failure(IllegalArgumentException("Use produtos de uma única empresa nesta demonstração."))
        }
        val itensPedido = itensCarrinho.map {
            ItemPedido(it.produto.produtoId, it.produto.nome, it.produto.precoUnitarioCentavos, it.quantidade)
        }
        val subtotal = itensPedido.sumOf { it.subtotalCentavos }
        val pedido = Pedido(
            id = java.util.UUID.randomUUID().toString(),
            chaveConfirmacao = chaveConfirmacao,
            clienteId = clienteId,
            empresaId = empresas.single(),
            itens = itensPedido,
            endereco = endereco,
            subtotalCentavos = subtotal,
            totalCentavos = subtotal,
            formaPagamento = formaPagamento,
            referenciaComprovante = referenciaComprovante,
            status = StatusPedido.RECEBIDO,
            criadoEmMillis = criadoEmMillis
        )
        return runCatching { salvar(pedido) }
    }
}
