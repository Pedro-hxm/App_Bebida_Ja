package br.com.bebidaja.data

data class DemoBeverage(
    val name: String,
    val description: String,
    val price: String
)

data class DemoStore(
    val name: String,
    val description: String
)

val demoCategories = listOf("Água", "Refrigerante", "Suco", "Energético")

val demoBeverages = listOf(
    DemoBeverage("Água mineral", "Garrafa de 1,5 L", "R$ 4,50"),
    DemoBeverage("Refrigerante", "Garrafa de 2 L", "R$ 9,90"),
    DemoBeverage("Suco de uva", "Caixa de 1 L", "R$ 7,50")
)

val demoStores = listOf(
    DemoStore("Adega Central", "Goiana • Entrega em 25–40 min"),
    DemoStore("Mercadinho do Bairro", "Goiana • Entrega em 30–50 min")
)