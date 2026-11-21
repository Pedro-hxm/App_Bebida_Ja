package br.com.bebidaja.data

import br.com.bebidaja.R

data class DemoBeverage(
    val name: String,
    val description: String,
    val price: String,
    val imageRes: Int
)

data class DemoStore(
    val name: String,
    val description: String
)

val demoCategories = listOf("Água", "Refrigerante", "Suco", "Energético")

val demoBeverages = listOf(
    DemoBeverage("Água mineral", "Garrafa de 1,5 L", "R$ 4,50", R.drawable.bebida_agua),
    DemoBeverage("Refrigerante", "Garrafa de 2 L", "R$ 9,90", R.drawable.bebida_refrigerante),
    DemoBeverage("Suco de uva", "Caixa de 1 L", "R$ 7,50", R.drawable.bebida_suco_uva)
)

val demoStores = listOf(
    DemoStore("Adega Central", "Goiana • Entrega em 25–40 min"),
    DemoStore("Mercadinho do Bairro", "Goiana • Entrega em 30–50 min")
)