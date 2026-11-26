package br.com.bebidaja.data

import br.com.bebidaja.R
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.Normalizer
import java.text.NumberFormat
import java.util.Locale

/**
 * Modelo temporário de catálogo. Os dados ficam apenas em memória e não representam
 * ofertas, preços ou disponibilidade de estabelecimentos reais.
 */
data class ProdutoDemonstrativo(
    val id: String,
    val empresaId: String,
    val nome: String?,
    val descricao: String? = null,
    val volume: String? = null,
    val categoria: String? = null,
    val precoCentavos: Long? = null,
    val disponivel: Boolean = true,
    val imagemRes: Int? = null,
    val imagemPath: String? = null
) {
    // Compatibilidade temporária com o card antigo, sem manter um segundo modelo de produto.
    val name: String get() = nome?.takeIf(String::isNotBlank) ?: "Não informado"
    val description: String
        get() = listOfNotNull(
            descricao?.takeIf(String::isNotBlank),
            volume?.takeIf(String::isNotBlank)?.let { "Volume: $it" },
            categoria?.takeIf(String::isNotBlank)?.let { "Categoria: $it" }
        ).joinToString(" • ").ifBlank { "Não informado" }
    val price: String get() = formatarPreco(precoCentavos)
    val imageRes: Int get() = imagemRes ?: R.drawable.mascote_bebida_ja
}

private data class ProdutoModeloDemo(
    val sufixoId: String,
    val nome: String?,
    val volume: String?,
    val categoria: String,
    val precoCentavos: Long?,
    val disponivel: Boolean = true,
    val imagemRes: Int? = null
)

private val modelosProdutosDemo = listOf(
    ProdutoModeloDemo(
        sufixoId = "agua",
        nome = "Água mineral (demo)",
        volume = "1,5 L",
        categoria = "Água",
        precoCentavos = 450,
        imagemRes = R.drawable.bebida_agua
    ),
    ProdutoModeloDemo(
        sufixoId = "refrigerante",
        nome = "Refrigerante (demo)",
        volume = "2 L",
        categoria = "Refrigerante",
        precoCentavos = 990,
        imagemRes = R.drawable.bebida_refrigerante
    ),
    ProdutoModeloDemo(
        sufixoId = "suco",
        nome = "Suco de uva (demo)",
        volume = "1 L",
        categoria = "Suco",
        precoCentavos = 750,
        imagemRes = R.drawable.bebida_suco_uva
    ),
    ProdutoModeloDemo(
        sufixoId = "energetico",
        nome = "Energético (demo)",
        volume = "269 ml",
        categoria = "Energético",
        precoCentavos = 850,
        disponivel = false
    ),
    ProdutoModeloDemo(
        sufixoId = "cerveja",
        nome = "Cerveja (demo)",
        volume = "350 ml",
        categoria = "Cerveja",
        precoCentavos = 650
    ),
    ProdutoModeloDemo(
        sufixoId = "destilado",
        nome = "Destilado (demo)",
        volume = "700 ml",
        categoria = "Destilados",
        precoCentavos = 3490
    ),
    ProdutoModeloDemo(
        sufixoId = "outros-sem-dados",
        nome = "Produto sem informações (demo)",
        volume = null,
        categoria = "Outros",
        precoCentavos = null,
        disponivel = false
    )
)

val demoProducts: List<ProdutoDemonstrativo> = demoStores.flatMap { empresa ->
    modelosProdutosDemo.map { modelo ->
        ProdutoDemonstrativo(
            id = "demo-${empresa.id}-${modelo.sufixoId}",
            empresaId = empresa.id,
            nome = modelo.nome,
            descricao = "Informações fictícias para demonstração local.",
            volume = modelo.volume,
            categoria = modelo.categoria,
            precoCentavos = modelo.precoCentavos,
            disponivel = modelo.disponivel,
            imagemRes = modelo.imagemRes
        )
    }
}

data class FiltroPesquisaCatalogo(
    val termo: String = "",
    val categoria: String? = null,
    val empresaId: String? = null
)

data class ResultadoPesquisaCatalogo(
    val empresas: List<DemoStore>,
    val produtos: List<ProdutoDemonstrativo>
)

/** Remove acentos e ignora diferenças de caixa e espaços externos. */
fun normalizarTextoPesquisa(valor: String): String =
    Normalizer.normalize(valor.trim().lowercase(Locale.ROOT), Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")

fun pesquisarCatalogo(
    produtos: List<ProdutoDemonstrativo>,
    empresas: List<DemoStore>,
    filtro: FiltroPesquisaCatalogo = FiltroPesquisaCatalogo()
): ResultadoPesquisaCatalogo {
    val termo = normalizarTextoPesquisa(filtro.termo)
    val empresasPermitidas = empresas.filter { empresa ->
        val selecionada = filtro.empresaId == null || empresa.id == filtro.empresaId
        val categoriaEncontrada = filtro.categoria == null ||
            produtos.any { produto ->
                produto.empresaId == empresa.id && produto.categoria == filtro.categoria
            }
        selecionada && categoriaEncontrada
    }
    val empresasEncontradas = empresasPermitidas.filter { empresa ->
        termo.isBlank() || normalizarTextoPesquisa(empresa.name).contains(termo)
    }
    val produtosEncontrados = produtos.filter { produto ->
        val empresa = empresas.firstOrNull { it.id == produto.empresaId }
        val empresaSelecionada = filtro.empresaId == null || produto.empresaId == filtro.empresaId
        val categoriaSelecionada = filtro.categoria == null || produto.categoria == filtro.categoria
        val termoEncontrado = termo.isBlank() ||
            normalizarTextoPesquisa(produto.nome.orEmpty()).contains(termo) ||
            normalizarTextoPesquisa(produto.descricao.orEmpty()).contains(termo) ||
            normalizarTextoPesquisa(produto.categoria.orEmpty()).contains(termo) ||
            empresa?.let { normalizarTextoPesquisa(it.name).contains(termo) } == true
        empresaSelecionada && categoriaSelecionada && termoEncontrado
    }

    return ResultadoPesquisaCatalogo(
        empresas = empresasEncontradas,
        produtos = produtosEncontrados
    )
}

fun validarCamposProduto(produto: ProdutoDemonstrativo): List<String> = buildList {
    if (produto.id.isBlank()) add("Identificador")
    if (produto.empresaId.isBlank()) add("Estabelecimento")
    if (produto.nome.isNullOrBlank()) add("Nome")
    if (produto.volume.isNullOrBlank()) add("Volume")
    if (produto.categoria !in demoCategories) add("Categoria")
    if (produto.precoCentavos == null || produto.precoCentavos <= 0L) add("Preço")
}

fun converterPrecoParaCentavos(valor: String): Long? {
    val informado = valor.trim()
    if (informado.isBlank()) return null
    val normalizado = if (informado.contains(',')) {
        informado.replace(".", "").replace(',', '.')
    } else {
        informado
    }
    return runCatching {
        val decimal = BigDecimal(normalizado).setScale(2, RoundingMode.UNNECESSARY)
        if (decimal <= BigDecimal.ZERO) return null
        decimal.multiply(BigDecimal(100)).longValueExact()
    }.getOrNull()
}

fun formatarPreco(centavos: Long?): String =
    centavos?.takeIf { it >= 0L }
        ?.let { NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(it / 100.0) }
        ?: "Não informado"

fun mapearProdutoParaCarrinho(produto: ProdutoDemonstrativo): ProdutoCarrinho? {
    val nome = produto.nome?.takeIf(String::isNotBlank) ?: return null
    val preco = produto.precoCentavos?.takeIf { it > 0L } ?: return null
    if (!produto.disponivel || produto.id.isBlank() || produto.empresaId.isBlank()) return null
    return ProdutoCarrinho(
        produtoId = produto.id,
        empresaId = produto.empresaId,
        nome = nome,
        precoUnitarioCentavos = preco
    )
}

fun podeAdicionarAoCarrinho(produto: ProdutoDemonstrativo, carrinho: Carrinho): Boolean {
    if (mapearProdutoParaCarrinho(produto) == null) return false
    val empresasNoCarrinho = carrinho.itens.map { it.produto.empresaId }.toSet()
    return empresasNoCarrinho.isEmpty() ||
        (empresasNoCarrinho.size == 1 && produto.empresaId in empresasNoCarrinho)
}

/**
 * Repositório isolado e volátil para prototipagem. A futura integração deve substituir
 * esta implementação pelo contrato compartilhado com Guilherme.
 */
class RepositorioCatalogoDemonstrativo(
    produtosIniciais: List<ProdutoDemonstrativo> = demoProducts
) {
    private val produtos = produtosIniciais.toMutableList()

    fun listarTodos(): List<ProdutoDemonstrativo> = produtos.toList()

    fun listarPorEmpresa(empresaId: String): List<ProdutoDemonstrativo> =
        produtos.filter { it.empresaId == empresaId }

    fun buscarPorId(produtoId: String): ProdutoDemonstrativo? =
        produtos.firstOrNull { it.id == produtoId }

    fun salvar(produto: ProdutoDemonstrativo): Boolean {
        if (validarCamposProduto(produto).isNotEmpty()) return false
        val indice = produtos.indexOfFirst { it.id == produto.id }
        if (indice >= 0) {
            if (produtos[indice].empresaId != produto.empresaId) return false
            produtos[indice] = produto
        } else {
            if (produtos.any { it.id == produto.id }) return false
            produtos.add(produto)
        }
        return true
    }

    fun alterarDisponibilidade(produtoId: String, empresaId: String, disponivel: Boolean): Boolean {
        val indice = produtos.indexOfFirst {
            it.id == produtoId && it.empresaId == empresaId
        }
        if (indice < 0) return false
        produtos[indice] = produtos[indice].copy(disponivel = disponivel)
        return true
    }

    fun remover(produtoId: String, empresaId: String): Boolean =
        produtos.removeAll { it.id == produtoId && it.empresaId == empresaId }
}
