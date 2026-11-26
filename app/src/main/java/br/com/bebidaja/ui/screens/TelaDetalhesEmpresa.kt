package br.com.bebidaja.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import br.com.bebidaja.data.Empresa
import br.com.bebidaja.data.ProdutoCatalogo
import br.com.bebidaja.ui.components.ImagemLocal

@Composable
fun TelaDetalhesEmpresa(
    empresa: Empresa?,
    produtos: List<ProdutoCatalogo>,
    onVerCatalogo: () -> Unit,
    onVoltar: () -> Unit
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            IconButton(onVoltar) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
            }
            Text(
                formatarCampoEmpresa(empresa?.nome).let {
                    if (empresa == null) "Estabelecimento não encontrado" else it
                },
                style = MaterialTheme.typography.headlineMedium
            )
        }
        if (empresa == null) {
            item { Text("Não encontramos esse estabelecimento.") }
        } else {
            item {
                ImagemLocal(
                    imagemPath = empresa.imagemPath,
                    contentDescription = "Imagem de ${empresa.nome}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }
            item { Text("Endereço: ${formatarCampoEmpresa(empresa.endereco)}") }
            item { Text("Região: ${formatarCampoEmpresa(empresa.regiao)}") }
            item { Text("Telefone: ${formatarCampoEmpresa(empresa.telefone)}") }
            item { Text("Funcionamento: ${formatarCampoEmpresa(empresa.horario)}") }
            item { Text("Entrega: ${formatarCampoEmpresa(empresa.areaEntrega)}") }
            item {
                Text(
                    "Produtos disponíveis: ${produtos.count { it.empresaId == empresa.id && it.disponivel }}"
                )
            }
            item {
                Button(onClick = onVerCatalogo, modifier = Modifier.fillMaxWidth()) {
                    Text("Ver catálogo")
                }
            }
        }
    }
}

internal fun formatarCampoEmpresa(valor: String?): String =
    valor?.trim()?.takeIf(String::isNotEmpty) ?: "Não informado"
