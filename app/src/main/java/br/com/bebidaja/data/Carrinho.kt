package br.com.bebidaja.data

/**
 * Modelo temporário para integrar o carrinho ao catálogo.
 * Substituir/adaptar ao contrato compartilhado com Arllan e Guilherme.
 */
data class ProdutoCarrinho(
    val produtoId: String,
    val empresaId: String,
    val nome: String,
    val precoUnitarioCentavos: Long
) {
    init {
        require(produtoId.isNotBlank()) { "O produto precisa de identificador." }
        require(empresaId.isNotBlank()) { "A empresa precisa de identificador." }
        require(nome.isNotBlank()) { "O produto precisa de nome." }
        require(precoUnitarioCentavos >= 0) { "O preço não pode ser negativo." }
    }
}

data class ItemCarrinho(
    val produto: ProdutoCarrinho,
    val quantidade: Int
) {
    val subtotalCentavos: Long
        get() = produto.precoUnitarioCentavos * quantidade
}

/**
 * Estado em memória do carrinho. Não depende de persistência nem de ViewModel
 * enquanto os contratos de arquitetura da equipe não estiverem definidos.
 */
class Carrinho {
    private val itensInternos = mutableListOf<ItemCarrinho>()

    val itens: List<ItemCarrinho>
        get() = itensInternos.toList()

    val subtotalCentavos: Long
        get() = itensInternos.sumOf { it.subtotalCentavos }

    val totalCentavos: Long
        get() = subtotalCentavos

    val estaVazio: Boolean
        get() = itensInternos.isEmpty()

    fun adicionar(produto: ProdutoCarrinho, quantidade: Int = 1) {
        require(quantidade > 0) { "A quantidade deve ser maior que zero." }

        val indice = itensInternos.indexOfFirst {
            it.produto.produtoId == produto.produtoId &&
                it.produto.empresaId == produto.empresaId
        }

        if (indice >= 0) {
            val item = itensInternos[indice]
            itensInternos[indice] = item.copy(quantidade = item.quantidade + quantidade)
        } else {
            itensInternos.add(ItemCarrinho(produto, quantidade))
        }
    }

    fun alterarQuantidade(produtoId: String, empresaId: String, quantidade: Int) {
        require(quantidade > 0) { "A quantidade deve ser maior que zero." }
        val indice = localizarItem(produtoId, empresaId)
        if (indice >= 0) {
            itensInternos[indice] = itensInternos[indice].copy(quantidade = quantidade)
        }
    }

    fun remover(produtoId: String, empresaId: String) {
        itensInternos.removeAll {
            it.produto.produtoId == produtoId && it.produto.empresaId == empresaId
        }
    }

    fun limpar() {
        itensInternos.clear()
    }

    private fun localizarItem(produtoId: String, empresaId: String): Int =
        itensInternos.indexOfFirst {
            it.produto.produtoId == produtoId && it.produto.empresaId == empresaId
        }
}
