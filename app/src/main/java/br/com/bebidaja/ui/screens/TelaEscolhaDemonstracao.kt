package br.com.bebidaja.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.bebidaja.data.TipoPerfil

@Composable
fun TelaEscolhaDemonstracao(
    onEscolher: (TipoPerfil) -> Unit,
    onAcessoReal: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("BebidaJá", style = MaterialTheme.typography.headlineMedium)
        Text("DEMONSTRAÇÃO", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
        Text("Escolha uma experiência para explorar com dados fictícios.")
        Button(onClick = { onEscolher(TipoPerfil.CLIENTE) }, modifier = Modifier.fillMaxWidth()) {
            Text("Explorar como cliente")
        }
        OutlinedButton(onClick = { onEscolher(TipoPerfil.EMPRESA) }, modifier = Modifier.fillMaxWidth()) {
            Text("Explorar como empresa")
        }
        TextButton(onClick = onAcessoReal, modifier = Modifier.fillMaxWidth()) {
            Text("Acessar conta ou cadastro")
        }
        Text("A demonstração é local e não cria uma conta.", style = MaterialTheme.typography.bodySmall)
    }
}
