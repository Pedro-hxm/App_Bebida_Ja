package br.com.bebidaja.data

interface RepositorioPedidos {
    fun buscarPorChaveConfirmacao(chave: String): Pedido?
    fun salvar(pedido: Pedido): Pedido
    fun listarPorCliente(clienteId: String): List<Pedido>
}

fun RepositorioPedidos.confirmarPedido(
    clienteId: String,
    carrinho: Carrinho,
    endereco: EnderecoEntrega?,
    formaPagamento: FormaPagamento?,
    referenciaComprovante: String,
    chaveConfirmacao: String,
    criadoEmMillis: Long = System.currentTimeMillis()
): Result<Pedido> {
    if (chaveConfirmacao.isBlank()) return Result.failure(IllegalArgumentException("Confirmação inválida."))
    val existente = runCatching { buscarPorChaveConfirmacao(chaveConfirmacao) }
        .getOrElse { return Result.failure(it) }
    existente?.let { return Result.success(it) }
    if (clienteId.isBlank() || carrinho.estaVazio || endereco?.estaValido != true || formaPagamento == null) {
        return Result.failure(IllegalArgumentException("Dados do pedido inválidos."))
    }
    if (!referenciaComprovante.startsWith("DEMO-")) {
        return Result.failure(IllegalArgumentException("O comprovante deve ser demonstrativo."))
    }
    val empresas = carrinho.itens.map { it.produto.empresaId }.distinct()
    if (empresas.size != 1) return Result.failure(IllegalArgumentException("Use produtos de uma única empresa."))
    val itens = carrinho.itens.map {
        ItemPedido(it.produto.produtoId, it.produto.nome, it.produto.precoUnitarioCentavos, it.quantidade,
            it.produto.volume, it.produto.imagemRes, it.produto.imagemPath)
    }
    val subtotal = itens.sumOf { it.subtotalCentavos }
    return runCatching { salvar(Pedido(
        id = java.util.UUID.randomUUID().toString(), chaveConfirmacao = chaveConfirmacao,
        clienteId = clienteId, empresaId = empresas.single(), itens = itens,
        endereco = endereco, subtotalCentavos = subtotal, totalCentavos = subtotal,
        formaPagamento = formaPagamento, referenciaComprovante = referenciaComprovante,
        status = StatusPedido.RECEBIDO, criadoEmMillis = criadoEmMillis
    )) }
}
