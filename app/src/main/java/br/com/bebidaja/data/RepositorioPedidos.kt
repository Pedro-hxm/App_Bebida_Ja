package br.com.bebidaja.data

interface RepositorioPedidos {
    fun buscarPorChaveConfirmacao(chave: String): Pedido?
    fun salvar(pedido: Pedido): Pedido
    fun listarPorCliente(clienteId: String): List<Pedido>
}
