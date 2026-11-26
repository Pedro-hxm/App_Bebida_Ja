package br.com.bebidaja.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.bebidaja.data.Empresa
import br.com.bebidaja.data.ProdutoCatalogo
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    empresas: List<Empresa>,
    produtos: List<ProdutoCatalogo>,
    onSearchClick: () -> Unit,
    onStoreClick: (String) -> Unit,
    onAddToCart: (ProdutoCatalogo) -> Unit
) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("BebidaJá", style = MaterialTheme.typography.headlineMedium) }
        item { Text("Encontre estabelecimentos e produtos cadastrados no dispositivo.") }
        item { Button(onSearchClick, Modifier.fillMaxWidth()) { Text("Buscar bebidas") } }
        item { Text("Estabelecimentos", style = MaterialTheme.typography.titleLarge) }
        if (empresas.isEmpty()) item { Text("Nenhum estabelecimento cadastrado ainda.") }
        items(empresas, key = { it.id }) { empresa ->
            Card(onClick = { onStoreClick(empresa.id) }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(empresa.nome, style = MaterialTheme.typography.titleMedium)
                    Text("${empresa.regiao} · ${empresa.areaEntrega}")
                    Text("${empresa.horario} · ${empresa.telefone}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item { Text("Catálogo", style = MaterialTheme.typography.titleLarge) }
        if (produtos.isEmpty()) item { Text("Nenhum produto disponível ainda.") }
        items(produtos.filter { it.disponivel }, key = { it.id }) { produto ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(produto.nome, style = MaterialTheme.typography.titleMedium)
                    Text("${produto.descricao} · ${produto.categoria}")
                    Text(NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(produto.precoCentavos / 100.0))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { onStoreClick(produto.empresaId) }) { Text("Ver empresa") }
                        Button(onClick = { onAddToCart(produto) }) { Text("Adicionar") }
                    }
                }
            }
        }
    }
}
