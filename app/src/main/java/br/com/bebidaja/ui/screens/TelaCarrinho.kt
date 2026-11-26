package br.com.bebidaja.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.bebidaja.data.Carrinho
import br.com.bebidaja.data.EnderecoEntrega
import br.com.bebidaja.data.FormaPagamento
import br.com.bebidaja.data.ItemCarrinho
import br.com.bebidaja.data.ProdutoCarrinho
import br.com.bebidaja.ui.components.ImagemLocal
import br.com.bebidaja.ui.theme.AzulClaro
import br.com.bebidaja.ui.theme.AzulEscuro
import br.com.bebidaja.ui.theme.CinzaTexto
import java.util.UUID
import java.text.NumberFormat
import java.util.Locale

/** Adaptador simples para manter o domínio sem dependência de ViewModel nesta etapa. */
class EstadoCarrinhoUi {
    val carrinho = Carrinho()
    var endereco by androidx.compose.runtime.mutableStateOf<EnderecoEntrega?>(null)
        private set
    var formaPagamento by androidx.compose.runtime.mutableStateOf<FormaPagamento?>(null)
        private set
    var revisao by mutableIntStateOf(0)
        private set
    var chaveConfirmacao by androidx.compose.runtime.mutableStateOf(UUID.randomUUID().toString())
        private set

    fun salvarEndereco(novoEndereco: EnderecoEntrega) {
        endereco = novoEndereco
    }

    fun selecionarFormaPagamento(forma: FormaPagamento) {
        formaPagamento = forma
    }

    fun adicionar(produto: ProdutoCarrinho) {
        carrinho.adicionar(produto)
        chaveConfirmacao = UUID.randomUUID().toString()
        revisao++
    }

    fun limparCarrinho() {
        carrinho.limpar()
        chaveConfirmacao = UUID.randomUUID().toString()
        revisao++
    }

    fun limparSessao() {
        limparCarrinho()
        endereco = null
        formaPagamento = null
    }

    fun alterarQuantidade(item: ItemCarrinho, quantidade: Int) {
        if (quantidade <= 0) carrinho.remover(item.produto.produtoId, item.produto.empresaId)
        else carrinho.alterarQuantidade(item.produto.produtoId, item.produto.empresaId, quantidade)
        chaveConfirmacao = UUID.randomUUID().toString()
        revisao++
    }
}

@Composable
fun TelaCarrinho(
    estado: EstadoCarrinhoUi,
    onContinuar: () -> Unit
) {
    estado.revisao // Observa alterações do carrinho para atualizar a tela.
    val itens = estado.carrinho.itens

    if (itens.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = AzulEscuro
                )
                Text("Seu carrinho está vazio", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Adicione bebidas para visualizar seu pedido.",
                    color = CinzaTexto,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Meu carrinho", style = MaterialTheme.typography.headlineMedium)
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(itens, key = { "${it.produto.empresaId}-${it.produto.produtoId}" }) { item ->
                ItemCarrinhoCard(item) { quantidade ->
                    estado.alterarQuantidade(item, quantidade)
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Total", style = MaterialTheme.typography.titleLarge)
            Text(
                formatarReais(estado.carrinho.totalCentavos),
                style = MaterialTheme.typography.titleLarge,
                color = AzulEscuro
            )
        }
        Button(
            onClick = onContinuar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar para endereço")
        }
    }
}

@Composable
private fun ItemCarrinhoCard(
    item: ItemCarrinho,
    onQuantidadeAlterada: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AzulClaro)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ImagemLocal(item.produto.imagemPath, contentDescription = "Imagem de ${item.produto.nome}",
                modifier = Modifier.size(80.dp), imagemResFallback = item.produto.imagemRes)
            Text(item.produto.nome, style = MaterialTheme.typography.titleMedium)
            item.produto.volume?.takeIf { it.isNotBlank() }?.let { Text("Volume: $it", color = CinzaTexto) }
            Text(
                "Preço unitário: ${formatarReais(item.produto.precoUnitarioCentavos)}",
                color = CinzaTexto
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = { onQuantidadeAlterada(item.quantidade - 1) },
                        modifier = Modifier.semantics { contentDescription = "Diminuir quantidade de ${item.produto.nome}" }
                    ) {
                        Text("−")
                    }
                    Text(item.quantidade.toString())
                    TextButton(
                        onClick = { onQuantidadeAlterada(item.quantidade + 1) },
                        modifier = Modifier.semantics { contentDescription = "Aumentar quantidade de ${item.produto.nome}" }
                    ) {
                        Text("+")
                    }
                }
                Text("Subtotal: ${formatarReais(item.subtotalCentavos)}")
            }
            TextButton(onClick = { onQuantidadeAlterada(0) }) {
                Text("Remover")
            }
        }
    }
}

private fun formatarReais(centavos: Long): String =
    NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(centavos / 100.0)
