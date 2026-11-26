package br.com.bebidaja.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.bebidaja.data.ProdutoDemonstrativo
import br.com.bebidaja.data.formatarPreco
import br.com.bebidaja.ui.theme.AzulClaro

@Composable
fun CardProduto(
    produto: ProdutoDemonstrativo,
    nomeEmpresa: String? = null,
    onDetalhes: () -> Unit,
    onAdicionar: () -> Boolean
) {
    var foiAdicionado by remember(produto.id) { mutableStateOf(false) }
    var mostrarErroCarrinho by remember(produto.id) { mutableStateOf(false) }
    Card(
        onClick = onDetalhes,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AzulClaro)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ImagemProduto(
                produto = produto,
                modifier = Modifier
                    .size(width = 88.dp, height = 104.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    produto.nome?.takeIf(String::isNotBlank) ?: "Não informado",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Volume: ${produto.volume?.takeIf(String::isNotBlank) ?: "Não informado"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Categoria: ${produto.categoria?.takeIf(String::isNotBlank) ?: "Não informado"}",
                    style = MaterialTheme.typography.bodySmall
                )
                nomeEmpresa?.takeIf(String::isNotBlank)?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    formatarPreco(produto.precoCentavos),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    if (produto.disponivel) "Disponível" else "Indisponível",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (produto.disponivel) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
                Button(
                    onClick = {
                        if (onAdicionar()) {
                            foiAdicionado = true
                            mostrarErroCarrinho = false
                        } else {
                            mostrarErroCarrinho = true
                        }
                    },
                    enabled = produto.disponivel &&
                        produto.precoCentavos != null &&
                        produto.precoCentavos > 0
                ) {
                    Text(
                        when {
                            !produto.disponivel -> "Indisponível"
                            produto.precoCentavos == null || produto.precoCentavos <= 0 ->
                                "Preço não informado"
                            foiAdicionado -> "Adicionado"
                            else -> "Adicionar ao carrinho"
                        }
                    )
                }
                if (mostrarErroCarrinho) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { mostrarErroCarrinho = false },
                        title = { Text("Carrinho já contém outro estabelecimento") },
                        text = {
                            Text(
                                "Cada pedido aceita produtos de um único estabelecimento. " +
                                    "Remova os itens atuais ou finalize antes de adicionar produtos " +
                                    "de outro estabelecimento."
                            )
                        },
                        confirmButton = {
                            androidx.compose.material3.TextButton(
                                onClick = { mostrarErroCarrinho = false }
                            ) {
                                Text("Entendi")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ImagemProduto(produto: ProdutoDemonstrativo, modifier: Modifier = Modifier) {
    val imagem = produto.imagemRes
    if (imagem != null) {
        Image(
            painter = painterResource(imagem),
            contentDescription = "Imagem de ${produto.nome?.takeIf(String::isNotBlank) ?: "produto"}",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Imagem não informada",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
