package br.com.bebidaja.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.bebidaja.data.FormaPagamento
import br.com.bebidaja.data.Pedido
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TelaComprovante(
    formaPagamento: FormaPagamento,
    totalCentavos: Long,
    onCriarPedido: () -> Result<Pedido>,
    onPedidoCriado: () -> Unit
) {
    var comprovanteEnviado by rememberSaveable { mutableStateOf(false) }
    var erroCriacao by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Comprovante", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Etapa demonstrativa: não envie um comprovante bancário real.",
            color = MaterialTheme.colorScheme.secondary
        )

        Button(
            onClick = { comprovanteEnviado = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (comprovanteEnviado) "Comprovante de demonstração enviado" else "Simular envio")
        }

        if (comprovanteEnviado) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Comprovante fictício", style = MaterialTheme.typography.titleLarge)
                    Text("Referência: DEMO-BEBIDAJA-001")
                    Text("Forma selecionada: ${formaPagamento.descricao}")
                    Text("Valor do pedido: ${formatarReais(totalCentavos)}")
                    Text(
                        "Este conteúdo é uma simulação acadêmica. Ele não confirma pagamento nem valida transações.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            if (erroCriacao != null) {
                Text(erroCriacao.orEmpty(), color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = {
                    onCriarPedido()
                        .onSuccess { onPedidoCriado() }
                        .onFailure { erroCriacao = it.message ?: "Não foi possível criar o pedido." }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Criar pedido demonstrativo")
            }
        }
    }
}

private fun formatarReais(centavos: Long): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(centavos / 100.0)
