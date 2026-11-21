package br.com.bebidaja.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SearchScreen() {
    PlaceholderScreen(
        title = "Buscar bebidas",
        message = "A busca e os filtros serão desenvolvidos nas próximas etapas."
    )
}

@Composable
fun OrdersScreen() {
    PlaceholderScreen(
        title = "Seus pedidos",
        message = "O acompanhamento de pedidos será desenvolvido nas próximas etapas."
    )
}

@Composable
fun ProfileScreen(onCompanyClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Perfil", style = MaterialTheme.typography.headlineMedium)
        Text("As opções de conta serão adicionadas durante o projeto.")
        Button(onClick = onCompanyClick) {
            Text("Área da empresa (demonstração)")
        }
    }
}

@Composable
fun CompanyScreen(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Área da empresa", style = MaterialTheme.typography.headlineMedium)
        Text("Cadastro comercial e painel serão desenvolvidos nas próximas etapas.")
        Button(onClick = onBackClick) {
            Text("Voltar")
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String, message: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(message)
    }
}