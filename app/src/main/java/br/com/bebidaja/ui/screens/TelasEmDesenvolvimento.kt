package br.com.bebidaja.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.bebidaja.data.Empresa
import br.com.bebidaja.data.ProdutoCatalogo
import br.com.bebidaja.data.RepositorioDadosLocais
import br.com.bebidaja.data.TipoPerfil
import br.com.bebidaja.data.Pedido
import br.com.bebidaja.data.StatusPedido
import br.com.bebidaja.data.converterPrecoParaCentavos
import br.com.bebidaja.ui.navigation.DemoSession
import br.com.bebidaja.ui.components.ImagemLocal
import br.com.bebidaja.ui.components.SeletorImagemLocal
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun SearchScreen(produtos: List<ProdutoCatalogo>, onAddToCart: (ProdutoCatalogo) -> Unit) {
    var consulta by rememberSaveable { mutableStateOf("") }
    val filtrados = produtos.filter { it.disponivel && (consulta.isBlank() ||
        it.nome.contains(consulta, true) || it.categoria.contains(consulta, true) || it.descricao.contains(consulta, true)) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Buscar bebidas", style = MaterialTheme.typography.headlineMedium) }
        item { OutlinedTextField(consulta, { consulta = it }, Modifier.fillMaxWidth(), label = { Text("Bebida ou categoria") }, singleLine = true) }
        if (filtrados.isEmpty()) item { Text("Nenhum produto disponível encontrado.") }
        items(filtrados, key = { it.id }) { ProdutoCard(it, onAddToCart) }
    }
}

@Composable
fun OrdersScreen(pedidos: List<Pedido>) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Histórico de pedidos", style = MaterialTheme.typography.headlineMedium) }
        if (pedidos.isEmpty()) item { Text("Você ainda não tem pedidos.") }
        items(pedidos, key = { it.id }) { PedidoCard(it) }
    }
}

@Composable
fun ProfileScreen(
    tipoPerfil: TipoPerfil?, demoSession: DemoSession?, mostrarSaida: Boolean,
    empresa: Empresa?, nome: String, email: String,
    onCompanyClick: () -> Unit, onOrdersClick: () -> Unit, onSignOutClick: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Perfil", style = MaterialTheme.typography.headlineMedium)
        if (demoSession != null) Text("Demonstração local")
        Text("Nome: ${empresa?.nome ?: nome.ifBlank { "Não informado" }}")
        Text("E-mail: ${empresa?.email ?: email.ifBlank { "Não informado" }}")
        Text(if (tipoPerfil == TipoPerfil.EMPRESA) "Perfil comercial" else "Conta de cliente")
        if (empresa != null) {
            ImagemLocal(empresa.imagemPath, "Imagem de ${empresa.nome}", Modifier.fillMaxWidth().height(180.dp))
            Text("Endereço: ${empresa.endereco}")
            Text("Telefone: ${empresa.telefone}")
            Text("Horário: ${empresa.horario}")
            Text("Área de entrega: ${empresa.areaEntrega}")
            Text("Região: ${empresa.regiao}")
            Button(onClick = onCompanyClick) { Text("Gerenciar empresa e pedidos") }
        } else if (tipoPerfil == TipoPerfil.CLIENTE) {
            Button(onClick = onOrdersClick) { Text("Ver meus pedidos") }
        }
        if (mostrarSaida) TextButton(onClick = onSignOutClick) { Text(if (demoSession != null) "Sair da demonstração" else "Sair da conta") }
    }
}

@Composable
fun CompanyScreen(
    repositorio: RepositorioDadosLocais,
    empresaInicial: Empresa,
    pedidos: List<Pedido>,
    aoAlterarDados: () -> Unit,
    onManageProductsClick: () -> Unit,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    var nome by remember(empresaInicial.id) { mutableStateOf(empresaInicial.nome) }
    var endereco by remember(empresaInicial.id) { mutableStateOf(empresaInicial.endereco) }
    var telefone by remember(empresaInicial.id) { mutableStateOf(empresaInicial.telefone) }
    var horario by remember(empresaInicial.id) { mutableStateOf(empresaInicial.horario) }
    var area by remember(empresaInicial.id) { mutableStateOf(empresaInicial.areaEntrega) }
    var regiao by remember(empresaInicial.id) { mutableStateOf(empresaInicial.regiao) }
    var imagemEmpresa by remember(empresaInicial.id) { mutableStateOf(empresaInicial.imagemPath) }
    var produtoNome by rememberSaveable { mutableStateOf("") }
    var descricao by rememberSaveable { mutableStateOf("") }
    var categoria by rememberSaveable { mutableStateOf("") }
    var preco by rememberSaveable { mutableStateOf("") }
    var imagemProduto by rememberSaveable { mutableStateOf<String?>(null) }
    var produtoEmEdicao by rememberSaveable { mutableStateOf<String?>(null) }
    var erro by rememberSaveable { mutableStateOf<String?>(null) }
    val produtos = repositorio.listarProdutos(empresaInicial.id)

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Painel da empresa", style = MaterialTheme.typography.headlineMedium) }
        item { SeletorImagemLocal("Imagem da empresa", "empresas", imagemEmpresa) { imagemEmpresa = it } }
        item { OutlinedTextField(nome, { nome = it }, Modifier.fillMaxWidth(), label = { Text("Nome comercial") }, singleLine = true) }
        item { OutlinedTextField(endereco, { endereco = it }, Modifier.fillMaxWidth(), label = { Text("Endereço") }, singleLine = true) }
        item { OutlinedTextField(telefone, { telefone = it }, Modifier.fillMaxWidth(), label = { Text("Telefone") }, singleLine = true) }
        item { OutlinedTextField(horario, { horario = it }, Modifier.fillMaxWidth(), label = { Text("Horário de funcionamento") }, singleLine = true) }
        item { OutlinedTextField(area, { area = it }, Modifier.fillMaxWidth(), label = { Text("Área de entrega") }, singleLine = true) }
        item { OutlinedTextField(regiao, { if (it.length <= 80) regiao = it }, Modifier.fillMaxWidth(), label = { Text("Região de atendimento") }, singleLine = true) }
        item {
            Button(onClick = {
                if (nome.isBlank() || endereco.isBlank() || telefone.filter(Char::isDigit).length < 10 || horario.isBlank() || area.isBlank() || regiao.isBlank()) {
                    erro = "Preencha os dados comerciais, região e telefone válido."
                } else {
                    val salvo = repositorio.salvarEmpresa(empresaInicial.copy(nome = nome.trim(), endereco = endereco.trim(), telefone = telefone.trim(), horario = horario.trim(), areaEntrega = area.trim(), regiao = regiao.trim(), imagemPath = imagemEmpresa))
                    erro = if (salvo) null else "Não foi possível salvar os dados da empresa."
                    if (salvo) aoAlterarDados()
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Salvar dados comerciais") }
        }
        item { Text("Produtos", style = MaterialTheme.typography.titleLarge) }
        item { Button(onClick = onManageProductsClick, modifier = Modifier.fillMaxWidth()) { Text("Abrir gerenciamento completo do catálogo") } }
        item { OutlinedTextField(produtoNome, { produtoNome = it }, Modifier.fillMaxWidth(), label = { Text("Nome do produto") }, singleLine = true) }
        item { OutlinedTextField(descricao, { descricao = it }, Modifier.fillMaxWidth(), label = { Text("Volume ou descrição") }, singleLine = true) }
        item { OutlinedTextField(categoria, { categoria = it }, Modifier.fillMaxWidth(), label = { Text("Categoria") }, singleLine = true) }
        item { OutlinedTextField(preco, { preco = it }, Modifier.fillMaxWidth(), label = { Text("Preço (ex.: 9,90)") }, singleLine = true) }
        item { SeletorImagemLocal("Imagem do produto (opcional)", "produtos", imagemProduto) { imagemProduto = it } }
        item {
            Button(onClick = {
                val centavos = converterPrecoParaCentavos(preco)
                if (produtoNome.isBlank() || descricao.isBlank() || categoria.isBlank() || centavos == null || centavos <= 0L) {
                    erro = "Informe nome, descrição, categoria e preço válido."
                } else {
                    val salvo = repositorio.salvarProduto(ProdutoCatalogo(produtoEmEdicao ?: UUID.randomUUID().toString(), empresaInicial.id, produtoNome.trim(), descricao.trim(), categoria.trim(), centavos, volume = descricao.trim(), imagemPath = imagemProduto))
                    if (salvo) {
                        produtoEmEdicao = null; produtoNome = ""; descricao = ""; categoria = ""; preco = ""; imagemProduto = null; erro = null; aoAlterarDados()
                    } else erro = "Confira os campos: use uma categoria cadastrada e preço positivo."
                }
            }, modifier = Modifier.fillMaxWidth()) { Text(if (produtoEmEdicao == null) "Cadastrar produto" else "Salvar produto") }
        }
        erro?.let { mensagem -> item { Text(mensagem, color = MaterialTheme.colorScheme.error) } }
        items(produtos, key = { it.id }) { produto ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ImagemLocal(produto.imagemPath, "Imagem de ${produto.nome}", Modifier.fillMaxWidth().height(160.dp), produto.imagemRes)
                    Text(produto.nome, style = MaterialTheme.typography.titleMedium)
                    Text("${produto.descricao} · ${produto.categoria} · ${formatarValor(produto.precoCentavos)}")
                    Text(if (produto.disponivel) "Disponível" else "Indisponível")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { produtoEmEdicao = produto.id; produtoNome = produto.nome; descricao = produto.descricao; categoria = produto.categoria; preco = "%.2f".format(Locale.forLanguageTag("pt-BR"), produto.precoCentavos / 100.0).replace('.', ','); imagemProduto = produto.imagemPath }) { Text("Editar") }
                        TextButton(onClick = { repositorio.definirDisponibilidade(produto.id, empresaInicial.id, !produto.disponivel); erro = "atualizado"; aoAlterarDados() }) { Text(if (produto.disponivel) "Pausar" else "Disponibilizar") }
                        TextButton(onClick = { repositorio.removerProduto(produto.id, empresaInicial.id); if (produtoEmEdicao == produto.id) produtoEmEdicao = null; erro = "atualizado"; aoAlterarDados() }) { Text("Remover") }
                    }
                }
            }
        }
        item { Text("Pedidos recebidos", style = MaterialTheme.typography.titleLarge) }
        if (pedidos.isEmpty()) item { Text("Nenhum pedido recebido ainda.") }
        items(pedidos, key = { it.id }) {
            PedidoCard(it, acaoStatus = {
                val proximo = when (it.status) {
                    StatusPedido.RECEBIDO -> StatusPedido.EM_PREPARACAO
                    StatusPedido.EM_PREPARACAO -> StatusPedido.SAIU_PARA_ENTREGA
                    StatusPedido.SAIU_PARA_ENTREGA -> StatusPedido.ENTREGUE
                    StatusPedido.ENTREGUE -> null
                }
                if (proximo != null) { repositorio.atualizarStatus(it.id, empresaInicial.id, proximo); aoAlterarDados() }
            })
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = onProfileClick) { Text("Meu perfil") }
                TextButton(onClick = onBackClick) { Text("Voltar") }
                TextButton(onClick = onSignOutClick) { Text("Sair") }
            }
        }
    }
}

@Composable
private fun PedidoCard(pedido: Pedido, acaoStatus: (() -> Unit)? = null) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Pedido #${pedido.id.take(8)}", style = MaterialTheme.typography.titleMedium)
            Text("${formatarData(pedido.criadoEmMillis)} · ${pedido.status.descricao}")
            pedido.itens.forEach { Text("${it.quantidade} × ${it.nomeProduto}") }
            Text("Total: ${formatarValor(pedido.totalCentavos)}")
            if (acaoStatus != null && pedido.status != StatusPedido.ENTREGUE) {
                val proximo = when (pedido.status) {
                    StatusPedido.RECEBIDO -> "Aceitar e preparar"
                    StatusPedido.EM_PREPARACAO -> "Saiu para entrega"
                    StatusPedido.SAIU_PARA_ENTREGA -> "Marcar como entregue"
                    StatusPedido.ENTREGUE -> "Concluído"
                }
                Button(onClick = acaoStatus) { Text(proximo) }
            }
        }
    }
}

@Composable
private fun ProdutoCard(produto: ProdutoCatalogo, onAddToCart: (ProdutoCatalogo) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(produto.nome, style = MaterialTheme.typography.titleMedium)
            Text("${produto.descricao} · ${produto.categoria}")
            Text(formatarValor(produto.precoCentavos))
            Button(onClick = { onAddToCart(produto) }) { Text("Adicionar ao carrinho") }
        }
    }
}

private fun formatarValor(centavos: Long): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(centavos / 100.0)

private fun formatarData(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR")).format(Date(millis))

private fun java.math.BigDecimal.longValueExactOrNull(): Long? =
    try { longValueExact() } catch (_: ArithmeticException) { null }
