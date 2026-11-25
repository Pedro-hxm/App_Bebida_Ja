package br.com.bebidaja.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.bebidaja.data.Carrinho
import br.com.bebidaja.data.EnderecoEntrega
import br.com.bebidaja.data.FormaPagamento
import br.com.bebidaja.data.ItemCarrinho
import br.com.bebidaja.data.ProdutoCarrinho
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

    fun salvarEndereco(novoEndereco: EnderecoEntrega) {
        endereco = novoEndereco
    }

    fun selecionarFormaPagamento(forma: FormaPagamento) {
        formaPagamento = forma
    }

    fun adicionar(produto: ProdutoCarrinho) {
        carrinho.adicionar(produto)
        revisao++
    }

    fun alterarQuantidade(item: ItemCarrinho, quantidade: Int) {
        if (quantidade <= 0) carrinho.remover(item.produto.produtoId, item.produto.empresaId)
        else carrinho.alterarQuantidade(item.produto.produtoId, item.produto.empresaId, quantidade)
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
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Seu carrinho está vazio", style = MaterialTheme.typography.headlineSmall)
            Text("Adicione bebidas para visualizar seu pedido.")
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
        Text(
            "Total: ${formatarReais(estado.carrinho.totalCentavos)}",
            style = MaterialTheme.typography.titleLarge
        )
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(item.produto.nome, style = MaterialTheme.typography.titleMedium)
            Text("Preço unitário: ${formatarReais(item.produto.precoUnitarioCentavos)}")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { onQuantidadeAlterada(item.quantidade - 1) }) {
                        Text("−")
                    }
                    Text(item.quantidade.toString())
                    TextButton(onClick = { onQuantidadeAlterada(item.quantidade + 1) }) {
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
