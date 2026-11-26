package br.com.bebidaja.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.bebidaja.data.TipoPerfil
import br.com.bebidaja.data.Pedido
import br.com.bebidaja.ui.navigation.DemoSession
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SearchScreen() {
    PlaceholderScreen(
        title = "Buscar bebidas",
        message = "A busca e os filtros serão desenvolvidos nas próximas etapas."
    )
}

@Composable
fun OrdersScreen(pedidos: List<Pedido>) {
    if (pedidos.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("Seus pedidos", style = MaterialTheme.typography.headlineMedium)
            Text("Você ainda não tem pedidos.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Histórico de pedidos", style = MaterialTheme.typography.headlineMedium) }
        items(pedidos, key = { it.id }) { pedido ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Pedido #${pedido.id.take(8)}", style = MaterialTheme.typography.titleMedium)
                    Text("Data: ${formatarDataPedido(pedido.criadoEmMillis)}")
                    Text("Status: ${pedido.status.descricao}")
                    pedido.itens.forEach { item ->
                        Text("${item.quantidade} × ${item.nomeProduto}")
                    }
                    Text("Total: ${formatarValorPedido(pedido.totalCentavos)}")
                }
            }
        }
    }
}

private fun formatarDataPedido(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR")).format(Date(millis))

private fun formatarValorPedido(centavos: Long): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(centavos / 100.0)

@Composable
fun ProfileScreen(
    tipoPerfil: TipoPerfil?,
    demoSession: DemoSession?,
    mostrarSaida: Boolean,
    onCompanyClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Perfil", style = MaterialTheme.typography.headlineMedium)
        if (demoSession != null) {
            Text("DEMONSTRAÇÃO — dados fictícios", color = MaterialTheme.colorScheme.primary)
            Text("Nome: ${demoSession.nome}")
            Text("E-mail: ${demoSession.email}")
        }
        Text(
            when (tipoPerfil) {
                TipoPerfil.CLIENTE -> "Conta de cliente"
                TipoPerfil.EMPRESA -> "Conta de empresa"
                null -> "As opções de conta serão adicionadas durante o projeto."
            }
        )
        if (demoSession == null || tipoPerfil == TipoPerfil.EMPRESA) {
            Button(onClick = onCompanyClick) {
                Text("Área da empresa (demonstração)")
            }
        }
        if (demoSession?.tipoPerfil == TipoPerfil.CLIENTE) {
            Button(onClick = onOrdersClick) { Text("Ver pedidos") }
        }
        if (mostrarSaida) {
            TextButton(onClick = onSignOutClick) {
                Text(if (demoSession != null) "Sair da demonstração" else "Sair da conta")
            }
        }
    }
}

@Composable
fun CompanyScreen(
    onBackClick: () -> Unit,
    demoSession: DemoSession?,
    mostrarSaida: Boolean,
    onProfileClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Área da empresa", style = MaterialTheme.typography.headlineMedium)
        if (demoSession != null) {
            Text("DEMONSTRAÇÃO — dados fictícios", color = MaterialTheme.colorScheme.primary)
            Text("Nome: ${demoSession.nome}")
            Text("E-mail: ${demoSession.email}")
            Text("Perfil: Empresa")
            TextButton(onClick = onProfileClick) { Text("Ver perfil demonstrativo") }
        }
        Text("Cadastro comercial e painel serão desenvolvidos nas próximas etapas.")
        Button(onClick = onBackClick) {
            Text("Voltar")
        }
        if (mostrarSaida) {
            TextButton(onClick = onSignOutClick) {
                Text(if (demoSession != null) "Sair da demonstração" else "Sair da conta")
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(message)
    }
}
