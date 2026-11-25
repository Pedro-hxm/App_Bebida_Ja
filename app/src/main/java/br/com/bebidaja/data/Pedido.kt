package br.com.bebidaja.data

enum class StatusPedido(val descricao: String) {
    RECEBIDO("Recebido"),
    EM_PREPARACAO("Em preparação"),
    SAIU_PARA_ENTREGA("Saiu para entrega"),
    ENTREGUE("Entregue")
}

data class ItemPedido(
    val produtoId: String,
    val nomeProduto: String,
    val precoUnitarioCentavos: Long,
    val quantidade: Int
) {
    val subtotalCentavos: Long
        get() = precoUnitarioCentavos * quantidade
}

data class Pedido(
    val id: String,
    val chaveConfirmacao: String,
    val clienteId: String,
    val empresaId: String,
    val itens: List<ItemPedido>,
    val endereco: EnderecoEntrega,
    val subtotalCentavos: Long,
    val totalCentavos: Long,
    val formaPagamento: FormaPagamento,
    val referenciaComprovante: String,
    val status: StatusPedido = StatusPedido.RECEBIDO,
    val criadoEmMillis: Long
)
