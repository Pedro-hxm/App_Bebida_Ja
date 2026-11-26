package br.com.bebidaja.ui.navigation

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.bebidaja.data.DadosComerciaisEmpresa
import br.com.bebidaja.data.Empresa
import br.com.bebidaja.data.FormaPagamento
import br.com.bebidaja.data.ProdutoCarrinho
import br.com.bebidaja.data.ProdutoCatalogo
import br.com.bebidaja.data.RepositorioAutenticacaoFirebase
import br.com.bebidaja.data.RepositorioDadosLocais
import br.com.bebidaja.data.TipoPerfil
import br.com.bebidaja.data.demoCategories
import br.com.bebidaja.data.demoStores
import br.com.bebidaja.data.findDemoStoreById
import br.com.bebidaja.data.mapearProdutoParaCarrinho
import br.com.bebidaja.data.podeAdicionarAoCarrinho
import br.com.bebidaja.data.paraDemoStore
import br.com.bebidaja.data.paraProdutoDemonstrativo
import br.com.bebidaja.data.paraProdutoLocal
import br.com.bebidaja.data.confirmarPedido
import br.com.bebidaja.ui.screens.CompanyScreen
import br.com.bebidaja.ui.screens.HomeScreen
import br.com.bebidaja.ui.screens.LoginScreen
import br.com.bebidaja.ui.screens.OrdersScreen
import br.com.bebidaja.ui.screens.ProfileScreen
import br.com.bebidaja.ui.screens.RegistrationScreen
import br.com.bebidaja.ui.screens.SplashScreen
import br.com.bebidaja.ui.screens.EstadoCarrinhoUi
import br.com.bebidaja.ui.screens.TelaCarrinho
import br.com.bebidaja.ui.screens.TelaCatalogoEmpresa
import br.com.bebidaja.ui.screens.TelaCheckout
import br.com.bebidaja.ui.screens.TelaComprovante
import br.com.bebidaja.ui.screens.TelaDetalhesEmpresa
import br.com.bebidaja.ui.screens.TelaDetalhesProduto
import br.com.bebidaja.ui.screens.TelaEnderecoEntrega
import br.com.bebidaja.ui.screens.TelaEscolhaDemonstracao
import br.com.bebidaja.ui.screens.TelaGerenciamentoProdutos
import br.com.bebidaja.ui.screens.TelaPesquisaCatalogo
import br.com.bebidaja.ui.theme.AzulClaro
import br.com.bebidaja.ui.theme.AzulEscuro
import br.com.bebidaja.ui.theme.AzulMedio
import br.com.bebidaja.ui.theme.Branco
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private data class AppTab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun NavegacaoAplicativo(firebaseDisponivel: Boolean) {
    val contexto = LocalContext.current
    val repositorio = remember { RepositorioDadosLocais(contexto) }
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    var usuarioAutenticado by remember { mutableStateOf(false) }
    var tipoPerfilAtual by remember { mutableStateOf<TipoPerfil?>(null) }
    var perfilDemonstracao by remember { mutableStateOf<TipoPerfil?>(null) }
    var dadosAlterados by rememberSaveable { mutableIntStateOf(0) }
    var nomeUsuario by rememberSaveable { mutableStateOf("") }
    var emailUsuario by rememberSaveable { mutableStateOf("") }
    val demoSession = perfilDemonstracao?.let(::criarDemoSession)
    val uidAtual = if (usuarioAutenticado) runCatching { FirebaseAuth.getInstance().currentUser?.uid }.getOrNull() else null
    val chaveSessao = chaveSessaoCarrinho(uidAtual, perfilDemonstracao?.valorFirestore)
    val estadoCarrinho = remember(chaveSessao) { EstadoCarrinhoUi() }
    val isBusiness = (tipoPerfilAtual ?: perfilDemonstracao) == TipoPerfil.EMPRESA
    val idEmpresa = when {
        perfilDemonstracao == TipoPerfil.EMPRESA -> "adega-central"
        tipoPerfilAtual == TipoPerfil.EMPRESA -> uidAtual
        else -> null
    }
    val empresaAtual = if (tipoPerfilAtual == TipoPerfil.EMPRESA && uidAtual != null) {
        repositorio.empresaDoUsuario(uidAtual) ?: idEmpresa?.let(repositorio::buscarEmpresa)
    } else idEmpresa?.let(repositorio::buscarEmpresa)
    val empresaAtualFinal = empresaAtual ?: if (tipoPerfilAtual == TipoPerfil.EMPRESA && uidAtual != null) {
        Empresa(uidAtual, uidAtual, FirebaseAuth.getInstance().currentUser?.displayName.orEmpty(), FirebaseAuth.getInstance().currentUser?.email.orEmpty(), "", "", "", "")
    } else null
    val listaEmpresas = remember(dadosAlterados) { repositorio.listarEmpresas() }
    val listaProdutos = remember(dadosAlterados) { repositorio.listarProdutos() }
    val produtosDemonstrativos = remember(listaProdutos) { listaProdutos.map(ProdutoCatalogo::paraProdutoDemonstrativo) }
    val empresasDemonstrativas = remember(listaEmpresas) { listaEmpresas.map(Empresa::paraDemoStore) }
    val empresaIdGerenciada = if (isBusiness) empresaAtual?.id else null
    val podeGerenciarProdutos = empresaIdGerenciada != null

    fun sairDaDemonstracao() {
        estadoCarrinho.limparSessao()
        RepositorioAutenticacaoFirebase.sair()
        perfilDemonstracao = null; dadosAlterados++
        navController.navigate("escolha") { popUpTo(navController.graph.id) { inclusive = true }; launchSingleTop = true }
    }

    fun entrarNoPerfil(tipo: TipoPerfil, nome: String = "", dados: DadosComerciaisEmpresa? = null) {
        estadoCarrinho.limparSessao()
        perfilDemonstracao = null; usuarioAutenticado = true; tipoPerfilAtual = tipo
        val user = runCatching { FirebaseAuth.getInstance().currentUser }.getOrNull()
        nomeUsuario = nome.ifBlank { user?.displayName.orEmpty() }; emailUsuario = user?.email.orEmpty()
        if (tipo == TipoPerfil.EMPRESA && user != null && dados != null) {
            val salvo = repositorio.salvarEmpresa(Empresa(user.uid, user.uid, dados.nome, user.email.orEmpty(), dados.endereco, dados.telefone, dados.horario, dados.areaEntrega, dados.regiao, dados.imagemPath))
            if (!salvo) Toast.makeText(contexto, "Não foi possível gravar o perfil comercial neste dispositivo.", Toast.LENGTH_LONG).show()
            dadosAlterados++
        }
        navController.navigate(if (tipo == TipoPerfil.EMPRESA) "company" else "home") {
            popUpTo(navController.graph.id) { inclusive = true }; launchSingleTop = true
        }
    }

    DisposableEffect(firebaseDisponivel, usuarioAutenticado, uidAtual) {
        val auth = if (firebaseDisponivel) runCatching { FirebaseAuth.getInstance() }.getOrNull() else null
        if (auth == null) return@DisposableEffect onDispose { }

        var uidAnterior = auth.currentUser?.uid
        val listener = FirebaseAuth.AuthStateListener { atualizacao ->
            val novoUid = atualizacao.currentUser?.uid
            if (novoUid != uidAnterior) {
                val trocarSessao = exigeReautenticacao(uidAnterior, novoUid, usuarioAutenticado)
                uidAnterior = novoUid
                if (trocarSessao) {
                    estadoCarrinho.limparSessao()
                    usuarioAutenticado = false
                    tipoPerfilAtual = null
                    perfilDemonstracao = null
                    nomeUsuario = ""
                    emailUsuario = ""
                    navController.navigate("login") {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    LaunchedEffect(firebaseDisponivel) {
        if (!firebaseDisponivel) return@LaunchedEffect
        val user = runCatching { FirebaseAuth.getInstance().currentUser }.getOrNull() ?: return@LaunchedEffect
        FirebaseFirestore.getInstance().collection("usuarios").document(user.uid).get()
            .addOnSuccessListener { document ->
                val tipo = TipoPerfil.entries.firstOrNull { it.valorFirestore == document.getString("tipoPerfil") }
                if (document.exists() && tipo != null) entrarNoPerfil(tipo, document.getString("nome").orEmpty())
                else { RepositorioAutenticacaoFirebase.sair(); usuarioAutenticado = false }
            }
            .addOnFailureListener { usuarioAutenticado = false }
    }

    val tabs = if (isBusiness) listOf(
        AppTab("company", "Empresa", Icons.Filled.Home), AppTab("profile", "Perfil", Icons.Filled.Person)
    ) else listOf(
        AppTab("home", "Início", Icons.Filled.Home), AppTab("search", "Pesquisa", Icons.Filled.Search),
        AppTab("orders", "Pedidos", Icons.AutoMirrored.Filled.List), AppTab("profile", "Perfil", Icons.Filled.Person)
    )
    fun encerrarSessao() {
        estadoCarrinho.limparSessao()
        RepositorioAutenticacaoFirebase.sair(); usuarioAutenticado = false; tipoPerfilAtual = null
        nomeUsuario = ""; emailUsuario = ""
        navController.navigate("login") { popUpTo(navController.graph.id) { inclusive = true }; launchSingleTop = true }
    }
    fun abrirDemonstracao(tipo: TipoPerfil) {
        estadoCarrinho.limparSessao()
        RepositorioAutenticacaoFirebase.sair()
        perfilDemonstracao = tipo; usuarioAutenticado = false; tipoPerfilAtual = null; dadosAlterados++
        navController.navigate(if (tipo == TipoPerfil.EMPRESA) "company" else "home") {
            popUpTo("escolha") { inclusive = true }; launchSingleTop = true
        }
    }
    fun adicionarAoCarrinho(produto: br.com.bebidaja.data.ProdutoDemonstrativo): Boolean {
        if (!podeAdicionarAoCarrinho(produto, estadoCarrinho.carrinho)) {
            Toast.makeText(contexto, "Escolha produtos de uma única empresa e verifique a disponibilidade.", Toast.LENGTH_SHORT).show()
            return false
        }
        val item = mapearProdutoParaCarrinho(produto) ?: return false
        estadoCarrinho.adicionar(item)
        return true
    }
    fun abrirDetalheProduto(id: String) { navController.navigate("produto-detalhe/${Uri.encode(id)}") }

    Scaffold(
        topBar = {
            if (tabs.any { it.route == currentRoute } || (!isBusiness && currentRoute in listOf("empresa/{empresaId}", "empresa/{empresaId}/catalogo", "produto-detalhe/{produtoId}"))) Row(
                Modifier.fillMaxWidth().statusBarsPadding().padding(end = 8.dp),
                horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically
            ) {
                if (demoSession != null) Text("DEMONSTRAÇÃO", color = AzulMedio, modifier = Modifier.weight(1f).padding(start = 24.dp))
                if (!isBusiness) FilledTonalIconButton(onClick = { navController.navigate("cart") { launchSingleTop = true } }, colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = AzulClaro, contentColor = AzulEscuro)) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = "Carrinho", tint = AzulEscuro)
                }
            }
        },
        bottomBar = {
            if (tabs.any { it.route == currentRoute } || (!isBusiness && currentRoute == "cart")) NavigationBar(containerColor = Branco, tonalElevation = 0.dp) {
                tabs.forEach { tab -> NavigationBarItem(
                    selected = currentRoute == tab.route, onClick = { navController.openTab(tab.route) },
                    icon = { Icon(tab.icon, contentDescription = tab.label) }, label = { Text(tab.label) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = AzulEscuro, selectedTextColor = AzulEscuro, indicatorColor = AzulClaro, unselectedIconColor = AzulMedio, unselectedTextColor = AzulMedio)
                ) }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = "splash", modifier = Modifier.padding(innerPadding)) {
            composable("splash") { SplashScreen {
                if (!usuarioAutenticado) navController.navigate("escolha") { popUpTo("splash") { inclusive = true }; launchSingleTop = true }
            } }
            composable("escolha") { TelaEscolhaDemonstracao(::abrirDemonstracao, { navController.navigate("login") { launchSingleTop = true } }) }
            composable("login") { LoginScreen(firebaseDisponivel, { navController.navigate("cadastro") }, {
                navController.navigate("escolha") { popUpTo("login") { inclusive = true }; launchSingleTop = true }
            }, { tipo -> entrarNoPerfil(tipo) }) }
            composable("cadastro") { RegistrationScreen(firebaseDisponivel, { navController.popBackStack() }, ::entrarNoPerfil) }
            composable("home") {
                HomeScreen(listaEmpresas, listaProdutos, { navController.openTab("search") },
                    { id -> navController.navigate("empresa/${Uri.encode(id)}") }, ::abrirDetalheProduto,
                    { produto -> adicionarAoCarrinho(produto.paraProdutoDemonstrativo()) })
            }
            composable("empresa/{empresaId}") { entry ->
                val id = entry.arguments?.getString("empresaId").orEmpty()
                TelaDetalhesEmpresa(repositorio.buscarEmpresa(id), remember(id, dadosAlterados) { repositorio.listarProdutos(id, true) },
                    { navController.navigate("empresa/${Uri.encode(id)}/catalogo") }, { navController.popBackStack() })
            }
            composable("empresa/{empresaId}/catalogo") { entry ->
                val id = entry.arguments?.getString("empresaId").orEmpty()
                val empresa = repositorio.buscarEmpresa(id)?.paraDemoStore()
                TelaCatalogoEmpresa(empresa, remember(id, dadosAlterados) { repositorio.listarProdutos(id).map(ProdutoCatalogo::paraProdutoDemonstrativo) },
                    { navController.popBackStack() }, ::abrirDetalheProduto, ::adicionarAoCarrinho)
            }
            composable("produto-detalhe/{produtoId}") { entry ->
                val id = entry.arguments?.getString("produtoId").orEmpty()
                val produto = repositorio.listarProdutos().firstOrNull { it.id == id }?.paraProdutoDemonstrativo()
                val empresa = produto?.let { repositorio.buscarEmpresa(it.empresaId)?.paraDemoStore() }
                TelaDetalhesProduto(produto, empresa, { navController.popBackStack() }, ::adicionarAoCarrinho)
            }
            composable("empresa/{empresaId}/produtos") { entry ->
                val id = entry.arguments?.getString("empresaId").orEmpty()
                if (podeGerenciarProdutos && id == empresaIdGerenciada) {
                    val empresa = repositorio.buscarEmpresa(id)
                    TelaGerenciamentoProdutos(
                        empresaId = id, empresa = empresa?.paraDemoStore(), nomeEmpresa = empresa?.nome ?: "Empresa",
                        produtos = repositorio.listarProdutos(id).map(ProdutoCatalogo::paraProdutoDemonstrativo),
                        onVoltar = { navController.popBackStack() },
                        onSalvarProduto = { produto -> val local = produto.paraProdutoLocal(); val salvo = local != null && repositorio.salvarProduto(local); if (salvo) dadosAlterados++; salvo },
                        onAlterarDisponibilidade = { productId, companyId, available -> val updated = companyId == id && repositorio.definirDisponibilidade(productId, id, available); if (updated) dadosAlterados++; updated },
                        onRemoverProduto = { productId, companyId -> val removed = companyId == id && repositorio.removerProduto(productId, id); if (removed) dadosAlterados++; removed }
                    )
                }
            }
            composable("search") {
                TelaPesquisaCatalogo(produtosDemonstrativos, empresasDemonstrativas,
                    { id -> navController.navigate("empresa/${Uri.encode(id)}") }, ::abrirDetalheProduto, ::adicionarAoCarrinho)
            }
            composable("cart") { if (!isBusiness) TelaCarrinho(estadoCarrinho) { if (!estadoCarrinho.carrinho.estaVazio) navController.navigate("endereco") } }
            composable("endereco") { if (!isBusiness) TelaEnderecoEntrega(estadoCarrinho.endereco, { estadoCarrinho.salvarEndereco(it) }, { navController.navigate("checkout") }) }
            composable("checkout") {
                val endereco = estadoCarrinho.endereco
                if (!isBusiness && endereco != null && !estadoCarrinho.carrinho.estaVazio) TelaCheckout(
                    estadoCarrinho.carrinho.itens, endereco, estadoCarrinho.formaPagamento, estadoCarrinho.carrinho.totalCentavos,
                    { estadoCarrinho.selecionarFormaPagamento(it) }, { navController.navigate("comprovante") }
                ) else LaunchedEffect(Unit) { navController.popBackStack("cart", false) }
            }
            composable("comprovante") {
                val forma = estadoCarrinho.formaPagamento
                if (!isBusiness && forma != null && !estadoCarrinho.carrinho.estaVazio) TelaComprovante(
                    forma, estadoCarrinho.carrinho.totalCentavos,
                    onCriarPedido = {
                        val result = repositorio.confirmarPedido("${uidAtual ?: "cliente-demonstracao"}", estadoCarrinho.carrinho, estadoCarrinho.endereco, forma,
                            "DEMO-BEBIDAJA-001", estadoCarrinho.chaveConfirmacao)
                        if (result.isSuccess) { estadoCarrinho.limparCarrinho(); dadosAlterados++ }
                        result
                    }, onPedidoCriado = { navController.navigate("orders") { popUpTo("home"); launchSingleTop = true } }
                ) else LaunchedEffect(Unit) { navController.popBackStack("cart", false) }
            }
            composable("orders") { if (!isBusiness) OrdersScreen(remember(uidAtual, dadosAlterados) { repositorio.listarPorCliente(uidAtual ?: "cliente-demonstracao") }) }
            composable("profile") {
                val authUser = runCatching { FirebaseAuth.getInstance().currentUser }.getOrNull()
                ProfileScreen(tipoPerfil = demoSession?.tipoPerfil ?: tipoPerfilAtual, demoSession = demoSession,
                    mostrarSaida = usuarioAutenticado || demoSession != null, empresa = empresaAtualFinal,
                    nome = authUser?.displayName.orEmpty().ifBlank { nomeUsuario.ifBlank { demoSession?.nome.orEmpty() } },
                    email = authUser?.email.orEmpty().ifBlank { emailUsuario.ifBlank { demoSession?.email.orEmpty() } },
                    onCompanyClick = { if (isBusiness) navController.navigate("company") { launchSingleTop = true } },
                    onOrdersClick = { if (!isBusiness) navController.openTab("orders") },
                    onSignOutClick = { if (demoSession != null) sairDaDemonstracao() else encerrarSessao() })
            }
            composable("company") {
                val empresa = empresaAtualFinal
                if (isBusiness && empresa != null) CompanyScreen(
                    repositorio = repositorio, empresaInicial = empresa,
                    pedidos = remember(empresa.id, dadosAlterados) { repositorio.listarPorEmpresa(empresa.id) },
                    aoAlterarDados = { dadosAlterados++ },
                    onManageProductsClick = { navController.navigate("empresa/${Uri.encode(empresa.id)}/produtos") },
                    onBackClick = { navController.popBackStack() },
                    onProfileClick = { navController.navigate("profile") { launchSingleTop = true } },
                    onSignOutClick = { if (demoSession != null) sairDaDemonstracao() else encerrarSessao() })
            }
        }
    }
}

private fun NavHostController.openTab(route: String) {
    if (currentDestination?.route == route) return
    navigate(route) { popUpTo(if (route == "company" || currentDestination?.route == "company") "company" else "home") { inclusive = false }; launchSingleTop = true }
}
