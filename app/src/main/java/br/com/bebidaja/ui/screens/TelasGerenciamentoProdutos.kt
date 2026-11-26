package br.com.bebidaja.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.bebidaja.R
import br.com.bebidaja.data.DemoStore
import br.com.bebidaja.data.ProdutoDemonstrativo
import br.com.bebidaja.data.converterPrecoParaCentavos
import br.com.bebidaja.data.demoCategories
import br.com.bebidaja.data.formatarPreco
import br.com.bebidaja.data.validarCamposProduto
import br.com.bebidaja.ui.components.CategoryChoice
import br.com.bebidaja.ui.components.ImagemLocal
import br.com.bebidaja.ui.components.SeletorImagemLocal
import br.com.bebidaja.ui.components.SectionTitle
import java.util.UUID

private val imagensLocaisDemonstrativas = listOf(
    "Sem imagem" to 0,
    "Água (demo)" to R.drawable.bebida_agua,
    "Refrigerante (demo)" to R.drawable.bebida_refrigerante,
    "Suco (demo)" to R.drawable.bebida_suco_uva
)

@Composable
fun TelaGerenciamentoProdutos(
    empresaId: String,
    empresa: DemoStore?,
    nomeEmpresa: String,
    produtos: List<ProdutoDemonstrativo>,
    onVoltar: () -> Unit,
    onSalvarProduto: (ProdutoDemonstrativo) -> Boolean,
    onAlterarDisponibilidade: (String, String, Boolean) -> Boolean,
    onRemoverProduto: (String, String) -> Boolean
) {
    var mostrandoFormulario by rememberSaveable { mutableStateOf(false) }
    var produtoEditado by androidx.compose.runtime.remember {
        mutableStateOf<ProdutoDemonstrativo?>(null)
    }
    var produtoParaExcluir by androidx.compose.runtime.remember {
        mutableStateOf<ProdutoDemonstrativo?>(null)
    }
    var mensagem by rememberSaveable { mutableStateOf<String?>(null) }

    BackHandler(enabled = mostrandoFormulario) {
        mostrandoFormulario = false
        produtoEditado = null
    }

    if (mostrandoFormulario) {
        key(produtoEditado?.id ?: "novo-produto") {
            TelaFormularioProduto(
                empresaId = empresaId,
                produto = produtoEditado,
                onCancelar = {
                    mostrandoFormulario = false
                    produtoEditado = null
                },
                onSalvar = { produto ->
                    if (onSalvarProduto(produto)) {
                        mensagem = "Produto salvo neste dispositivo."
                        mostrandoFormulario = false
                        produtoEditado = null
                        true
                    } else {
                        mensagem = "Não foi possível salvar. Confira os campos e tente novamente."
                        false
                    }
                }
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                    Column {
                        Text("Gerenciar bebidas", style = MaterialTheme.typography.headlineSmall)
                        Text(nomeEmpresa, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            item {
                Text(
                    "Os produtos são salvos neste dispositivo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            mensagem?.let { texto ->
                item {
                    Text(
                        texto,
                        color = if (texto.startsWith("Não foi possível")) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            item {
                Button(
                    onClick = {
                        mensagem = null
                        produtoEditado = null
                        mostrandoFormulario = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cadastrar bebida")
                }
            }
            if (produtos.isEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SectionTitle("Nenhuma bebida cadastrada")
                        Text("Cadastre um produto para exibi-lo no catálogo demonstrativo.")
                    }
                }
            } else {
                items(produtos, key = { it.id }) { produto ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ImagemLocal(produto.imagemPath, "Imagem de ${produto.name}", Modifier.fillMaxWidth().height(150.dp), produto.imagemRes)
                            Text(
                                produto.nome?.takeIf(String::isNotBlank) ?: "Não informado",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Volume: ${produto.volume?.takeIf(String::isNotBlank) ?: "Não informado"}"
                            )
                            Text(
                                "Categoria: ${produto.categoria?.takeIf(String::isNotBlank) ?: "Não informado"}"
                            )
                            Text(formatarPreco(produto.precoCentavos))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(if (produto.disponivel) "Disponível" else "Indisponível")
                                Switch(
                                    checked = produto.disponivel,
                                    onCheckedChange = { disponivel ->
                                        mensagem = if (
                                            onAlterarDisponibilidade(
                                                produto.id,
                                                produto.empresaId,
                                                disponivel
                                            )
                                        ) {
                                            "Disponibilidade salva neste dispositivo."
                                        } else {
                                            "Não foi possível atualizar a disponibilidade."
                                        }
                                    }
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        mensagem = null
                                        produtoEditado = produto
                                        mostrandoFormulario = true
                                    }
                                ) {
                                    Text("Editar")
                                }
                                TextButton(onClick = { produtoParaExcluir = produto }) {
                                    Text("Remover")
                                }
                            }
                        }
                    }
                }
            }
            if (empresa == null) {
                item {
                    Text(
                        "Este perfil ainda não possui dados comerciais integrados. O catálogo é temporário.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    produtoParaExcluir?.let { produto ->
        AlertDialog(
            onDismissRequest = { produtoParaExcluir = null },
            title = { Text("Remover bebida?") },
            text = {
                Text(
                    "A bebida será removida do catálogo demonstrativo desta sessão. " +
                        "Esta ação não pode ser desfeita."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val removido = onRemoverProduto(produto.id, produto.empresaId)
                        mensagem = if (removido) {
                            "Produto removido do catálogo demonstrativo."
                        } else {
                            "Não foi possível remover o produto."
                        }
                        produtoParaExcluir = null
                    }
                ) {
                    Text("Remover")
                }
            },
            dismissButton = {
                TextButton(onClick = { produtoParaExcluir = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TelaFormularioProduto(
    empresaId: String,
    produto: ProdutoDemonstrativo?,
    onCancelar: () -> Unit,
    onSalvar: (ProdutoDemonstrativo) -> Boolean
) {
    var nome by rememberSaveable(produto?.id) {
        mutableStateOf(produto?.nome.orEmpty())
    }
    var volume by rememberSaveable(produto?.id) {
        mutableStateOf(produto?.volume.orEmpty())
    }
    var categoria by rememberSaveable(produto?.id) {
        mutableStateOf(produto?.categoria?.takeIf { it in demoCategories } ?: demoCategories.first())
    }
    var preco by rememberSaveable(produto?.id) {
        mutableStateOf(produto?.precoCentavos?.let(::formatarPrecoParaCampo).orEmpty())
    }
    var descricao by rememberSaveable(produto?.id) {
        mutableStateOf(produto?.descricao.orEmpty())
    }
    var imagemSelecionada by rememberSaveable(produto?.id) {
        mutableIntStateOf(produto?.imagemRes ?: 0)
    }
    var imagemPath by rememberSaveable(produto?.id) { mutableStateOf(produto?.imagemPath) }
    var erros by rememberSaveable(produto?.id) { mutableStateOf(emptySet<String>()) }
    var erroAoSalvar by rememberSaveable(produto?.id) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onCancelar) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
            }
            Text(
                if (produto == null) "Cadastrar bebida" else "Editar bebida",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        Text(
            "As alterações são persistidas localmente neste aparelho.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it; erros = emptySet() },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nome *") },
            isError = "Nome" in erros,
            supportingText = if ("Nome" in erros) ({ Text("Informe o nome da bebida.") }) else null,
            singleLine = true
        )
        OutlinedTextField(
            value = volume,
            onValueChange = { volume = it; erros = emptySet() },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Volume *") },
            isError = "Volume" in erros,
            supportingText = if ("Volume" in erros) ({ Text("Informe o volume.") }) else null,
            singleLine = true
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Categoria *", style = MaterialTheme.typography.labelLarge)
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(demoCategories, key = { it }) { opcao ->
                    CategoryChoice(
                        label = opcao,
                        selected = categoria == opcao,
                        onClick = { categoria = opcao; erros = emptySet() }
                    )
                }
            }
            if ("Categoria" in erros) {
                Text(
                    "Selecione uma categoria.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        OutlinedTextField(
            value = preco,
            onValueChange = { preco = it; erros = emptySet() },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Preço em reais *") },
            placeholder = { Text("Ex.: 9,90") },
            isError = "Preço" in erros,
            supportingText = if ("Preço" in erros) {
                ({ Text("Informe um preço maior que zero, com até duas casas decimais.") })
            } else {
                null
            },
            singleLine = true
        )
        OutlinedTextField(
            value = descricao,
            onValueChange = { descricao = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Descrição (opcional)") },
            minLines = 2
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Imagem (opcional)", style = MaterialTheme.typography.labelLarge)
            SeletorImagemLocal("Escolher imagem do dispositivo", "produtos", imagemPath) { imagemPath = it }
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(imagensLocaisDemonstrativas, key = { it.first }) { (rotulo, imagemRes) ->
                    CategoryChoice(
                        label = rotulo,
                        selected = imagemSelecionada == imagemRes,
                        onClick = { imagemSelecionada = imagemRes }
                    )
                }
            }
            Text(
                "As imagens locais são ilustrativas e não representam marcas comerciais.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (erroAoSalvar) {
            Text(
                "Não foi possível salvar o produto. Tente novamente.",
                color = MaterialTheme.colorScheme.error
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onCancelar, modifier = Modifier.weight(1f)) {
                Text("Cancelar")
            }
            Button(
                onClick = {
                    val candidato = ProdutoDemonstrativo(
                        id = produto?.id ?: "produto-demo-${UUID.randomUUID()}",
                        empresaId = empresaId,
                        nome = nome.trim(),
                        descricao = descricao.trim().ifBlank { null },
                        volume = volume.trim(),
                        categoria = categoria,
                        precoCentavos = converterPrecoParaCentavos(preco),
                        disponivel = produto?.disponivel ?: true,
                        imagemRes = imagemSelecionada.takeIf { it != 0 },
                        imagemPath = imagemPath
                    )
                    erros = validarCamposProduto(candidato).toSet()
                    if (erros.isEmpty()) {
                        erroAoSalvar = !onSalvar(candidato)
                    } else {
                        erroAoSalvar = false
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Salvar")
            }
        }
    }
}

private fun formatarPrecoParaCampo(centavos: Long): String {
    val reais = centavos / 100
    val fracao = (centavos % 100).toString().padStart(2, '0')
    return "$reais,$fracao"
}
