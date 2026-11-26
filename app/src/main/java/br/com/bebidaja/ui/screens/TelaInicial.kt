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
import br.com.bebidaja.data.Empresa
import br.com.bebidaja.data.ProdutoCatalogo
import br.com.bebidaja.data.demoCategories
import br.com.bebidaja.data.filtrarEmpresasPorRegiao
import br.com.bebidaja.data.listarRegioesPersistidas
import br.com.bebidaja.data.paraDemoStore
import br.com.bebidaja.data.paraProdutoDemonstrativo
import br.com.bebidaja.ui.components.CardProduto
import br.com.bebidaja.ui.components.CategoryChoice
import br.com.bebidaja.ui.components.SectionTitle
import br.com.bebidaja.ui.components.StoreCard
import br.com.bebidaja.ui.theme.AzulClaro

@Composable
fun HomeScreen(
    empresas: List<Empresa>,
    produtos: List<ProdutoCatalogo>,
    onSearchClick: () -> Unit,
    onStoreClick: (String) -> Unit,
    onProductClick: (String) -> Unit,
    onAddToCart: (ProdutoCatalogo) -> Boolean
) {
    var categoriaSelecionada by rememberSaveable { mutableStateOf<String?>(null) }
    var regiaoSelecionada by rememberSaveable { mutableStateOf<String?>(null) }
    val regioes = listarRegioesPersistidas(empresas)
    val filtradas = filtrarEmpresasPorRegiao(empresas, regiaoSelecionada)
    val idsPermitidos = filtradas.mapTo(mutableSetOf()) { it.id }
    val produtosFiltrados = produtos.filter {
        it.disponivel &&
            it.empresaId in idsPermitidos &&
            (categoriaSelecionada == null || it.categoria == categoriaSelecionada)
    }

    LazyColumn(Modifier.fillMaxWidth(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Image(painterResource(R.drawable.mascote_bebida_ja), "Logo do BebidaJá", Modifier.size(48.dp), contentScale = ContentScale.Fit)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("BebidaJá", style = MaterialTheme.typography.titleLarge)
                    Text("Encontre bebidas e estabelecimentos perto de você.")
                }
            }
        }
        item {
            Card(onClick = onSearchClick, Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = AzulClaro)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.primary)
                    Text("Buscar bebidas ou estabelecimentos", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionTitle("Categorias")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { CategoryChoice("Todas", categoriaSelecionada == null) { categoriaSelecionada = null } }
                    items(demoCategories, key = { it }) { categoria ->
                        CategoryChoice(categoria, categoriaSelecionada == categoria) {
                            categoriaSelecionada = if (categoriaSelecionada == categoria) null else categoria
                        }
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionTitle("Estabelecimentos")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { CategoryChoice("Todas as regiões", regiaoSelecionada == null) { regiaoSelecionada = null } }
                    items(regioes, key = { it.id }) { regiao ->
                        CategoryChoice(
                            regiao.nome,
                            regiaoSelecionada == regiao.id
                        ) {
                            regiaoSelecionada =
                                if (regiaoSelecionada == regiao.id) null else regiao.id
                        }
                    }
                }
            }
        }
        if (filtradas.isEmpty()) item { Text("Nenhum estabelecimento nesta região.") }
        else items(filtradas, key = { it.id }) { empresa -> StoreCard(empresa.paraDemoStore()) { onStoreClick(empresa.id) } }
        item { SectionTitle("Bebidas em destaque") }
        if (produtosFiltrados.isEmpty()) item { Text("Nenhuma bebida disponível nesta categoria.") }
        else items(produtosFiltrados, key = { it.id }) { produto ->
            val empresa = empresas.firstOrNull { it.id == produto.empresaId }
            CardProduto(
                produto = produto.paraProdutoDemonstrativo(), nomeEmpresa = empresa?.nome,
                onDetalhes = { onProductClick(produto.id) },
                onAdicionar = { onAddToCart(produto) }
            )
        }
    }
}
