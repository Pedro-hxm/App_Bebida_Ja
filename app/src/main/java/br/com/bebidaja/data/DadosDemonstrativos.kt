package br.com.bebidaja.data

import java.util.Locale

/** Compatibilidade de apresentação para os componentes de produto já existentes. */
typealias DemoBeverage = ProdutoDemonstrativo

data class DemoRegion(
    val id: String,
    val name: String
)

data class RegiaoPersistida(
    val id: String,
    val nome: String
)

/** Adaptador de apresentação; os estabelecimentos vêm de RepositorioDadosLocais. */
data class DemoStore(
    val id: String,
    val name: String,
    val description: String,
    val imageRes: Int? = null,
    val address: String? = null,
    val phone: String? = null,
    val hours: String? = null,
    val deliveryArea: String? = null,
    val regionId: String? = null,
    val imagePath: String? = null,
    val regionName: String? = null
)

val demoRegions = listOf(
    DemoRegion(id = "goiana", name = "Goiana")
)

val demoCategories = listOf(
    "Água",
    "Refrigerante",
    "Suco",
    "Energético",
    "Cerveja",
    "Destilados",
    "Outros"
)

private fun chaveRegiao(valor: String?): String? {
    val normalizado = valor?.trim()?.takeIf(String::isNotBlank) ?: return null
    return normalizarTextoPesquisa(normalizado)
        .replace("\\s*\\(demo\\)$".toRegex(), "")
        .replace("-demo$".toRegex(), "")
        .replace("[^a-z0-9]+".toRegex(), "-")
        .trim('-')
        .takeIf(String::isNotBlank)
}

fun resolverIdRegiao(nomeRegiao: String?): String? {
    val nome = nomeRegiao?.trim()?.takeIf(String::isNotBlank) ?: return null
    val chave = chaveRegiao(nome) ?: return nome
    return demoRegions.firstOrNull { regiao ->
        chaveRegiao(regiao.id) == chave || chaveRegiao(regiao.name) == chave
    }?.id ?: nome
}

fun listarRegioesPersistidas(empresas: List<Empresa>): List<RegiaoPersistida> {
    val regioes = linkedMapOf<String, RegiaoPersistida>()
    empresas.forEach { empresa ->
        val nome = empresa.regiao.trim().takeIf(String::isNotBlank) ?: return@forEach
        val id = resolverIdRegiao(nome) ?: return@forEach
        val chave = chaveRegiao(id) ?: return@forEach
        regioes.putIfAbsent(chave, RegiaoPersistida(id, nome))
    }
    return regioes.values.sortedBy { it.nome.lowercase(Locale.ROOT) }
}

fun filtrarEmpresasPorRegiao(
    empresas: List<Empresa>,
    regiaoId: String?
): List<Empresa> {
    val chaveSelecionada = chaveRegiao(regiaoId) ?: return empresas
    return empresas.filter { chaveRegiao(it.regiao) == chaveSelecionada }
}

fun findDemoRegionName(regionId: String?): String? {
    val chave = chaveRegiao(regionId) ?: return null
    return demoRegions.firstOrNull { regiao ->
        chaveRegiao(regiao.id) == chave || chaveRegiao(regiao.name) == chave
    }?.name ?: regionId?.trim()?.takeIf(String::isNotBlank)
}

fun demoStoreRegionLabel(regionId: String?): String =
    findDemoRegionName(regionId)
        ?.let { regionName -> "Região: $regionName" }
        ?: "Região não informada"

fun demoStoreRegionLabel(store: DemoStore): String =
    store.regionName?.takeIf(String::isNotBlank)?.let { "Região: $it" }
        ?: demoStoreRegionLabel(store.regionId)
