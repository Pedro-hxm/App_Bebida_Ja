package br.com.bebidaja.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import br.com.bebidaja.R
import br.com.bebidaja.data.demoBeverages
import br.com.bebidaja.data.demoCategories
import br.com.bebidaja.data.demoRegions
import br.com.bebidaja.data.demoStores
import br.com.bebidaja.data.filterDemoStoresByRegion
import br.com.bebidaja.ui.components.BeverageCard
import br.com.bebidaja.ui.components.CategoryChoice
import br.com.bebidaja.ui.components.SectionTitle
import br.com.bebidaja.ui.components.StoreCard
import br.com.bebidaja.ui.theme.AzulClaro

@Composable
fun HomeScreen(
    onSearchClick: () -> Unit,
    onStoreClick: (String) -> Unit,
    onAddToCart: (br.com.bebidaja.data.DemoBeverage) -> Unit
) {
    var selectedCategory by rememberSaveable { mutableStateOf(demoCategories.first()) }
    var selectedRegionId by rememberSaveable { mutableStateOf<String?>(null) }
    val filteredStores = filterDemoStoresByRegion(demoStores, selectedRegionId)

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.mascote_bebida_ja),
                    contentDescription = "Logo do BebidaJá",
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Fit
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("BebidaJá", style = MaterialTheme.typography.titleLarge)
                    Text("Encontre bebidas perto de você.")
                }
            }
        }
        item {
            Card(
                onClick = onSearchClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AzulClaro)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Buscar bebidas ou estabelecimentos",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionTitle("Categorias")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(demoCategories) { category ->
                        CategoryChoice(
                            label = category,
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                    }
                }
                Text(
                    "Seleção visual de demonstração",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionTitle("Estabelecimentos")
                Text(
                    "Filtre por região. Estabelecimentos, regiões e estimativas são demonstrativos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item(key = "todas-as-regioes") {
                        CategoryChoice(
                            label = "Todas",
                            selected = selectedRegionId == null,
                            onClick = { selectedRegionId = null }
                        )
                    }
                    items(demoRegions, key = { it.id }) { region ->
                        CategoryChoice(
                            label = region.name,
                            selected = selectedRegionId == region.id,
                            onClick = { selectedRegionId = region.id }
                        )
                    }
                }
            }
        }
        if (filteredStores.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Nenhum estabelecimento nesta região",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Tente outra região ou selecione “Todas”.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(filteredStores, key = { it.id }) { store ->
                StoreCard(store, onClick = { onStoreClick(store.id) })
            }
        }
        item { SectionTitle("Bebidas em destaque") }
        items(demoBeverages) { beverage ->
            BeverageCard(beverage) { onAddToCart(beverage) }
        }
        item {
            Text(
                "Conteúdo ilustrativo para a base visual.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}