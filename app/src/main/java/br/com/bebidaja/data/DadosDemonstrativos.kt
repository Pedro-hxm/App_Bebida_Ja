package br.com.bebidaja.data

import br.com.bebidaja.R

data class DemoBeverage(
    val name: String,
    val description: String,
    val price: String,
    val imageRes: Int
)

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
    val regionId: String? = null
)

val demoRegions = listOf(
    DemoRegion(id = "goiana-demo", name = "Goiana (demo)"),
    DemoRegion(id = "sem-resultados-demo", name = "Sem resultados (demo)")
)

val demoCategories = listOf("Água", "Refrigerante", "Suco", "Energético")

val demoBeverages = listOf(
    DemoBeverage("Água mineral", "Garrafa de 1,5 L", "R$ 4,50", R.drawable.bebida_agua),
    DemoBeverage("Refrigerante", "Garrafa de 2 L", "R$ 9,90", R.drawable.bebida_refrigerante),
    DemoBeverage("Suco de uva", "Caixa de 1 L", "R$ 7,50", R.drawable.bebida_suco_uva)
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

fun demoStoreRegionLabel(regionId: String?): String =
    demoRegions.firstOrNull { region -> region.id == regionId }
        ?.let { region -> "Região: " + region.name }
        ?: "Região não informada"