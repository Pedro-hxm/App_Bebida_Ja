package br.com.bebidaja.data

import br.com.bebidaja.R
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.Normalizer
import java.text.NumberFormat
import java.util.Locale

/** Modelo de apresentação adaptado dos registros persistidos do catálogo local. */
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
        val empresa = empresas.firstOrNull { it.id == produto.empresaId } ?: return@filter false
        val empresaSelecionada = filtro.empresaId == null || produto.empresaId == filtro.empresaId
        val categoriaSelecionada = filtro.categoria == null || produto.categoria == filtro.categoria
        val termoEncontrado = termo.isBlank() ||
            normalizarTextoPesquisa(produto.nome.orEmpty()).contains(termo) ||
            normalizarTextoPesquisa(produto.descricao.orEmpty()).contains(termo) ||
            normalizarTextoPesquisa(produto.categoria.orEmpty()).contains(termo) ||
            normalizarTextoPesquisa(empresa.name).contains(termo)
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
        ?.let { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(it / 100.0) }
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
