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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.bebidaja.data.DemoStore
import br.com.bebidaja.data.FiltroPesquisaCatalogo
import br.com.bebidaja.data.ProdutoDemonstrativo
import br.com.bebidaja.data.demoCategories
import br.com.bebidaja.data.pesquisarCatalogo
import br.com.bebidaja.ui.components.CardProduto
import br.com.bebidaja.ui.components.CategoryChoice
import br.com.bebidaja.ui.components.ImagemProduto
import br.com.bebidaja.ui.components.SectionTitle
import br.com.bebidaja.ui.components.StoreCard
import br.com.bebidaja.data.formatarPreco

@Composable
fun TelaCatalogoEmpresa(
    empresa: DemoStore?,
    produtos: List<ProdutoDemonstrativo>,
    onVoltar: () -> Unit,
    onProdutoClick: (String) -> Unit,
    onAdicionarAoCarrinho: (ProdutoDemonstrativo) -> Boolean
) {
    var categoriaSelecionada by rememberSaveable(empresa?.id) {
        mutableStateOf<String?>(null)
    }
    val produtosVisiveis = produtos.filter {
        categoriaSelecionada == null || it.categoria == categoriaSelecionada
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVoltar) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                }
                Column {
                    Text(
                        empresa?.name ?: "Estabelecimento não encontrado",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text("Catálogo de bebidas", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        item {
            Text(
                "Dados armazenados localmente neste dispositivo, sem sincronização entre dispositivos. Preços e disponibilidade não têm integração comercial definitiva.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (empresa == null) {
            item { EstadoVazioCatalogo("Não encontramos esse estabelecimento.") }
        } else {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item(key = "todas-categorias") {
                        CategoryChoice(
                            label = "Todas",
                            selected = categoriaSelecionada == null,
                            onClick = { categoriaSelecionada = null }
                        )
                    }
                    items(demoCategories, key = { it }) { categoria ->
                        CategoryChoice(
                            label = categoria,
                            selected = categoriaSelecionada == categoria,
                            onClick = { categoriaSelecionada = categoria }
                        )
                    }
                }
            }
            if (produtosVisiveis.isEmpty()) {
                item {
                    EstadoVazioCatalogo(
                        "Nenhum produto nesta categoria",
                        "Escolha outra categoria para ver os itens disponíveis."
                    )
                }
            } else {
                items(produtosVisiveis, key = { it.id }) { produto ->
                    CardProduto(
                        produto = produto,
                        onDetalhes = { onProdutoClick(produto.id) },
                        onAdicionar = { onAdicionarAoCarrinho(produto) }
                    )
                }
            }
        }
    }
}

@Composable
fun TelaDetalhesProduto(
    produto: ProdutoDemonstrativo?,
    empresa: DemoStore?,
    onVoltar: () -> Unit,
    onAdicionarAoCarrinho: (ProdutoDemonstrativo) -> Boolean
) {
    if (produto == null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onVoltar) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
            }
            EstadoVazioCatalogo("Não encontramos esse produto.")
        }
        return
    }

    val scrollState = rememberScrollState()
    var feedback by rememberSaveable(produto.id) { mutableStateOf<String?>(null) }
    LaunchedEffect(feedback) {
        if (feedback != null) scrollState.animateScrollTo(scrollState.maxValue)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVoltar) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
            }
            Text("Detalhes do produto", style = MaterialTheme.typography.titleLarge)
        }
        ImagemProduto(
            produto = produto,
            modifier = Modifier.fillMaxWidth().height(220.dp)
        )
        Text(
            produto.nome?.takeIf(String::isNotBlank) ?: "Não informado",
            style = MaterialTheme.typography.headlineSmall
        )
        Text("Estabelecimento: ${empresa?.name ?: "Não informado"}")
        Text("Volume: ${produto.volume?.takeIf(String::isNotBlank) ?: "Não informado"}")
        Text("Categoria: ${produto.categoria?.takeIf(String::isNotBlank) ?: "Não informado"}")
        Text(
            formatarPreco(produto.precoCentavos),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            if (produto.disponivel) "Disponibilidade: disponível" else "Disponibilidade: indisponível",
            color = if (produto.disponivel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        produto.descricao?.takeIf(String::isNotBlank)?.let { Text(it) }
        Text(
            "Informações demonstrativas. A disponibilidade definitiva depende da integração comercial.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = {
                feedback = if (onAdicionarAoCarrinho(produto)) {
                    "Adicionado ao carrinho."
                } else {
                    "O carrinho aceita itens de um estabelecimento por pedido."
                }
            },
            enabled = produto.disponivel &&
                produto.precoCentavos != null &&
                produto.precoCentavos > 0,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                when {
                    !produto.disponivel -> "Produto indisponível"
                    produto.precoCentavos == null || produto.precoCentavos <= 0 ->
                        "Preço não informado"
                    else -> "Adicionar ao carrinho"
                }
            )
        }
        feedback?.let { texto ->
            Text(
                texto,
                color = if (texto.startsWith("Adicionado")) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }
}

@Composable
fun TelaPesquisaCatalogo(
    produtos: List<ProdutoDemonstrativo>,
    empresas: List<DemoStore>,
    onEmpresaClick: (String) -> Unit,
    onProdutoClick: (String) -> Unit,
    onAdicionarAoCarrinho: (ProdutoDemonstrativo) -> Boolean
) {
    var termo by rememberSaveable { mutableStateOf("") }
    var categoriaSelecionada by rememberSaveable { mutableStateOf<String?>(null) }
    var empresaSelecionadaId by rememberSaveable { mutableStateOf<String?>(null) }
    val resultado = pesquisarCatalogo(
        produtos = produtos,
        empresas = empresas,
        filtro = FiltroPesquisaCatalogo(
            termo = termo,
            categoria = categoriaSelecionada,
            empresaId = empresaSelecionadaId
        )
    )
    val filtrosAtivos = termo.isNotBlank() ||
        categoriaSelecionada != null ||
        empresaSelecionadaId != null

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Buscar bebidas", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Pesquise por bebida, categoria ou estabelecimento. Resultados demonstrativos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            OutlinedTextField(
                value = termo,
                onValueChange = { termo = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Bebida ou estabelecimento") },
                singleLine = true
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle("Categorias")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item(key = "todas-categorias") {
                        CategoryChoice(
                            label = "Todas",
                            selected = categoriaSelecionada == null,
                            onClick = { categoriaSelecionada = null }
                        )
                    }
                    items(demoCategories, key = { it }) { categoria ->
                        CategoryChoice(
                            label = categoria,
                            selected = categoriaSelecionada == categoria,
                            onClick = { categoriaSelecionada = categoria }
                        )
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle("Estabelecimentos")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item(key = "todos-estabelecimentos") {
                        CategoryChoice(
                            label = "Todos",
                            selected = empresaSelecionadaId == null,
                            onClick = { empresaSelecionadaId = null }
                        )
                    }
                    items(empresas, key = { it.id }) { empresa ->
                        CategoryChoice(
                            label = empresa.name,
                            selected = empresaSelecionadaId == empresa.id,
                            onClick = {
                                empresaSelecionadaId =
                                    if (empresaSelecionadaId == empresa.id) null else empresa.id
                            }
                        )
                    }
                }
            }
        }
        if (filtrosAtivos) {
            item {
                TextButton(
                    onClick = {
                        termo = ""
                        categoriaSelecionada = null
                        empresaSelecionadaId = null
                    }
                ) {
                    Text("Limpar pesquisa e filtros")
                }
            }
        }
        if (resultado.empresas.isEmpty() && resultado.produtos.isEmpty()) {
            item {
                EstadoVazioCatalogo(
                    "Nenhum resultado encontrado",
                    "Tente outro termo ou limpe os filtros selecionados."
                )
            }
        } else {
            if (resultado.empresas.isNotEmpty()) {
                item { SectionTitle("Estabelecimentos") }
                items(resultado.empresas, key = { "empresa-${it.id}" }) { empresa ->
                    StoreCard(empresa) { onEmpresaClick(empresa.id) }
                }
            }
            if (resultado.produtos.isNotEmpty()) {
                item { SectionTitle("Bebidas") }
                items(resultado.produtos, key = { "produto-${it.id}" }) { produto ->
                    val nomeEmpresa = empresas.firstOrNull { it.id == produto.empresaId }?.name
                    CardProduto(
                        produto = produto,
                        nomeEmpresa = nomeEmpresa,
                        onDetalhes = { onProdutoClick(produto.id) },
                        onAdicionar = { onAdicionarAoCarrinho(produto) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EstadoVazioCatalogo(
    titulo: String,
    descricao: String = "Não há dados para exibir."
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(titulo, style = MaterialTheme.typography.titleMedium)
        Text(
            descricao,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
