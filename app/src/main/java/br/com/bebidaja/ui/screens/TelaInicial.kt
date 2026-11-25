package br.com.bebidaja.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import br.com.bebidaja.R
import br.com.bebidaja.data.demoBeverages
import br.com.bebidaja.data.demoCategories
import br.com.bebidaja.data.demoStores
import br.com.bebidaja.ui.components.BeverageCard
import br.com.bebidaja.ui.components.CategoryChoice
import br.com.bebidaja.ui.components.SectionTitle
import br.com.bebidaja.ui.components.StoreCard
import br.com.bebidaja.ui.theme.AzulClaro

@Composable
fun HomeScreen(
    onSearchClick: () -> Unit,
    onAddToCart: (br.com.bebidaja.data.DemoBeverage) -> Unit
) {
    var selectedCategory by rememberSaveable { mutableStateOf(demoCategories.first()) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onSearchClick),
                colors = CardDefaults.cardColors(containerColor = AzulClaro)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Filled.Search, contentDescription = "Buscar")
                    Text("Buscar bebidas ou estabelecimentos")
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
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        item { SectionTitle("Estabelecimentos") }
        items(demoStores) { store -> StoreCard(store) }
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