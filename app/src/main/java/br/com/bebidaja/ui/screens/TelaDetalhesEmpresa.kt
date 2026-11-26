package br.com.bebidaja.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.bebidaja.data.Empresa
import br.com.bebidaja.data.ProdutoCatalogo
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TelaDetalhesEmpresa(empresa: Empresa?, produtos: List<ProdutoCatalogo>, onVerCatalogo: () -> Unit, onVoltar: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            IconButton(onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") }
            Text(empresa?.nome ?: "Estabelecimento não encontrado", style = MaterialTheme.typography.headlineMedium)
        }
        if (empresa == null) item { Text("Não encontramos esse estabelecimento.") } else {
            item { Text("${empresa.endereco} · ${empresa.regiao}") }
            item { Text("Telefone: ${empresa.telefone}") }
            item { Text("Funcionamento: ${empresa.horario}") }
            item { Text("Entrega: ${empresa.areaEntrega}") }
            item { Text("Produtos disponíveis: ${produtos.count { it.empresaId == empresa.id && it.disponivel }}") }
            item { Button(onClick = onVerCatalogo, modifier = Modifier.fillMaxWidth()) { Text("Ver catálogo") } }
        }
    }
}
