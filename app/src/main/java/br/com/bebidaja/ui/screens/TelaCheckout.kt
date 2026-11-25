package br.com.bebidaja.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.bebidaja.data.EnderecoEntrega
import br.com.bebidaja.data.FormaPagamento
import br.com.bebidaja.data.ItemCarrinho
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TelaCheckout(
    itens: List<ItemCarrinho>,
    endereco: EnderecoEntrega,
    pagamentoInicial: FormaPagamento?,
    totalCentavos: Long,
    onConfirmar: (FormaPagamento) -> Unit
) {
    var formaSelecionadaNome by rememberSaveable {
        mutableStateOf(pagamentoInicial?.name ?: FormaPagamento.PIX.name)
    }
    var confirmado by rememberSaveable { mutableStateOf(false) }
    val formaSelecionada = FormaPagamento.valueOf(formaSelecionadaNome)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Checkout", style = MaterialTheme.typography.headlineMedium)

        Text("Itens do pedido", style = MaterialTheme.typography.titleLarge)
        itens.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${item.quantidade} × ${item.produto.nome}")
                Text(formatarReais(item.subtotalCentavos))
            }
        }

        Text("Endereço de entrega", style = MaterialTheme.typography.titleLarge)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("${endereco.rua}, ${endereco.numero}")
                Text("${endereco.bairro} • ${endereco.cidade}")
                Text("CEP: ${endereco.cep}")
                if (endereco.complemento.isNotBlank()) {
                    Text("Complemento: ${endereco.complemento}")
                }
            }
        }

        Text("Forma de pagamento", style = MaterialTheme.typography.titleLarge)
        FormaPagamento.entries.forEach { forma ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        formaSelecionadaNome = forma.name
                        confirmado = false
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = formaSelecionada == forma,
                        onClick = {
                            formaSelecionadaNome = forma.name
                            confirmado = false
                        }
                    )
                    Text(forma.descricao)
                }
            }
        }

        Text(
            "Simulação acadêmica: nenhuma cobrança será realizada e não informe dados bancários.",
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            "Total: ${formatarReais(totalCentavos)}",
            style = MaterialTheme.typography.headlineSmall
        )

        if (confirmado) {
            Text(
                "Forma de pagamento registrada para esta demonstração.",
                color = MaterialTheme.colorScheme.primary
            )
        }

        Button(
            onClick = {
                onConfirmar(formaSelecionada)
                confirmado = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirmar dados do checkout")
        }
    }
}

private fun formatarReais(centavos: Long): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(centavos / 100.0)
