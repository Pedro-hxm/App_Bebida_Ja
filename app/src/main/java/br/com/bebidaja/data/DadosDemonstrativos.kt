package br.com.bebidaja.data

/** Alias temporário para o modelo estruturado do catálogo demonstrativo. */
typealias DemoBeverage = ProdutoDemonstrativo

data class DemoRegion(
    val id: String,
    val name: String
)

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
    DemoRegion(id = "goiana-demo", name = "Goiana (demo)")
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

val demoStores = listOf(
    DemoStore(
        id = "adega-central",
        name = "Adega Central",
        description = "Entrega em 25–40 min",
        regionId = "goiana-demo"
    ),
    DemoStore(
        id = "mercadinho-do-bairro",
        name = "Mercadinho do Bairro",
        description = "Entrega em 30–50 min",
        regionId = "goiana-demo"
    )
)

fun findDemoStoreById(storeId: String): DemoStore? =
    demoStores.firstOrNull { it.id == storeId }

fun filterDemoStoresByRegion(
    stores: List<DemoStore>,
    regionId: String?
): List<DemoStore> =
    if (regionId == null) stores else stores.filter { it.regionId == regionId }

fun findDemoRegionName(regionId: String?): String? =
    demoRegions.firstOrNull { region -> region.id == regionId }?.name

fun demoStoreRegionLabel(regionId: String?): String =
    findDemoRegionName(regionId)
        ?.let { regionName -> "Região: " + regionName }
        ?: "Região não informada"

fun demoStoreRegionLabel(store: DemoStore): String =
    store.regionName?.takeIf(String::isNotBlank)?.let { "Região: $it" }
        ?: demoStoreRegionLabel(store.regionId)
