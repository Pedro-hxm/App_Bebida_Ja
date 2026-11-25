package br.com.bebidaja.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.bebidaja.data.EnderecoEntrega

@Composable
fun TelaEnderecoEntrega(
    enderecoInicial: EnderecoEntrega?,
    onSalvar: (EnderecoEntrega) -> Unit,
    onContinuar: () -> Unit
) {
    var rua by rememberSaveable { mutableStateOf(enderecoInicial?.rua.orEmpty()) }
    var numero by rememberSaveable { mutableStateOf(enderecoInicial?.numero.orEmpty()) }
    var bairro by rememberSaveable { mutableStateOf(enderecoInicial?.bairro.orEmpty()) }
    var cidade by rememberSaveable { mutableStateOf(enderecoInicial?.cidade.orEmpty()) }
    var cep by rememberSaveable { mutableStateOf(enderecoInicial?.cep.orEmpty()) }
    var complemento by rememberSaveable { mutableStateOf(enderecoInicial?.complemento.orEmpty()) }
    var tentouSalvar by rememberSaveable { mutableStateOf(false) }
    var enderecoSalvo by remember { mutableStateOf(enderecoInicial != null) }

    val endereco = EnderecoEntrega(rua, numero, bairro, cidade, cep, complemento)
    val camposBasicosPreenchidos = rua.isNotBlank() &&
        numero.isNotBlank() && bairro.isNotBlank() && cidade.isNotBlank()
    val cepValido = cep.filter(Char::isDigit).length == 8

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Endereço de entrega", style = MaterialTheme.typography.headlineMedium)
        Text("Informe onde o pedido deverá ser entregue.")

        OutlinedTextField(
            value = rua,
            onValueChange = { rua = it; enderecoSalvo = false },
            label = { Text("Rua") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = numero,
            onValueChange = { numero = it; enderecoSalvo = false },
            label = { Text("Número") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = bairro,
            onValueChange = { bairro = it; enderecoSalvo = false },
            label = { Text("Bairro") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = cidade,
            onValueChange = { cidade = it; enderecoSalvo = false },
            label = { Text("Cidade") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = cep,
            onValueChange = { cep = it; enderecoSalvo = false },
            label = { Text("CEP") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = complemento,
            onValueChange = { complemento = it; enderecoSalvo = false },
            label = { Text("Complemento (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (tentouSalvar && !camposBasicosPreenchidos) {
            Text(
                "Preencha rua, número, bairro e cidade.",
                color = MaterialTheme.colorScheme.error
            )
        } else if (tentouSalvar && !cepValido) {
            Text("Informe um CEP com 8 números.", color = MaterialTheme.colorScheme.error)
        }

        if (enderecoSalvo) {
            Text(
                "Endereço salvo para este fluxo demonstrativo.",
                color = MaterialTheme.colorScheme.primary
            )
        }

        Button(
            onClick = {
                tentouSalvar = true
                if (endereco.estaValido) {
                    onSalvar(endereco)
                    enderecoSalvo = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (enderecoSalvo) "Atualizar endereço" else "Salvar endereço")
        }
        Button(
            onClick = onContinuar,
            enabled = enderecoSalvo,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar para checkout")
        }
    }
}
