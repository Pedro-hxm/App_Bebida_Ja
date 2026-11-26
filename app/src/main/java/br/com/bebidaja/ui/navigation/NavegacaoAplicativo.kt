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
import br.com.bebidaja.data.RepositorioAutenticacaoFirebase
import br.com.bebidaja.data.RepositorioDadosLocais
import br.com.bebidaja.data.TipoPerfil
import br.com.bebidaja.ui.screens.CompanyScreen
import br.com.bebidaja.ui.screens.HomeScreen
import br.com.bebidaja.ui.screens.LoginScreen
import br.com.bebidaja.ui.screens.OrdersScreen
import br.com.bebidaja.ui.screens.ProfileScreen
import br.com.bebidaja.ui.screens.RegistrationScreen
import br.com.bebidaja.ui.screens.SearchScreen
import br.com.bebidaja.ui.screens.SplashScreen
import br.com.bebidaja.ui.screens.EstadoCarrinhoUi
import br.com.bebidaja.ui.screens.TelaCarrinho
import br.com.bebidaja.ui.screens.TelaCheckout
import br.com.bebidaja.ui.screens.TelaComprovante
import br.com.bebidaja.ui.screens.TelaDetalhesEmpresa
import br.com.bebidaja.ui.screens.TelaEnderecoEntrega
import br.com.bebidaja.ui.screens.TelaEscolhaDemonstracao
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
    var usuarioAutenticado by rememberSaveable { mutableStateOf(false) }
    var tipoPerfilAtual by rememberSaveable { mutableStateOf<TipoPerfil?>(null) }
    var perfilDemonstracao by rememberSaveable { mutableStateOf<TipoPerfil?>(null) }
    var geracaoDemonstracao by rememberSaveable { mutableIntStateOf(0) }
    var dadosAlterados by rememberSaveable { mutableIntStateOf(0) }
    var nomeUsuario by rememberSaveable { mutableStateOf("") }
    var emailUsuario by rememberSaveable { mutableStateOf("") }
    val demoSession = perfilDemonstracao?.let(::criarDemoSession)
    val estadoCarrinho = remember(geracaoDemonstracao) { EstadoCarrinhoUi() }
    val uidAtual = if (usuarioAutenticado) runCatching { FirebaseAuth.getInstance().currentUser?.uid }.getOrNull() else null
    val idEmpresa = when {
        perfilDemonstracao == TipoPerfil.EMPRESA -> "adega-central"
        tipoPerfilAtual == TipoPerfil.EMPRESA -> uidAtual
        else -> null
    }
    val empresaAtual = idEmpresa?.let { repositorio.buscarEmpresa(it) } ?: if (tipoPerfilAtual == TipoPerfil.EMPRESA && uidAtual != null) {
        Empresa(uidAtual, uidAtual, FirebaseAuth.getInstance().currentUser?.displayName.orEmpty(), FirebaseAuth.getInstance().currentUser?.email.orEmpty(), "", "", "", "")
    } else null
    val clienteId = uidAtual ?: "cliente-demonstracao"
    val listaEmpresas = remember(dadosAlterados) { repositorio.listarEmpresas() }
    val listaProdutos = remember(dadosAlterados) { repositorio.listarProdutos(somenteDisponiveis = true) }

    fun sairDaDemonstracao() {
        perfilDemonstracao = null; geracaoDemonstracao++; dadosAlterados++
        navController.navigate("escolha") { popUpTo(navController.graph.id) { inclusive = true }; launchSingleTop = true }
    }

    fun entrarNoPerfil(tipo: TipoPerfil, nome: String = "", dados: DadosComerciaisEmpresa? = null) {
        perfilDemonstracao = null; usuarioAutenticado = true; tipoPerfilAtual = tipo
        val user = runCatching { FirebaseAuth.getInstance().currentUser }.getOrNull()
        nomeUsuario = nome.ifBlank { user?.displayName.orEmpty() }
        emailUsuario = user?.email.orEmpty()
        if (tipo == TipoPerfil.EMPRESA && user != null && dados != null) {
            repositorio.salvarEmpresa(Empresa(user.uid, user.uid, dados.nome, user.email.orEmpty(), dados.endereco, dados.telefone, dados.horario, dados.areaEntrega))
            dadosAlterados++
        }
        navController.navigate(if (tipo == TipoPerfil.EMPRESA) "company" else "home") {
            popUpTo(navController.graph.id) { inclusive = true }; launchSingleTop = true
        }
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

    val isBusiness = (tipoPerfilAtual ?: perfilDemonstracao) == TipoPerfil.EMPRESA
    val tabs = if (isBusiness) listOf(
        AppTab("company", "Empresa", Icons.Filled.Home), AppTab("profile", "Perfil", Icons.Filled.Person)
    ) else listOf(
        AppTab("home", "Início", Icons.Filled.Home), AppTab("search", "Pesquisa", Icons.Filled.Search),
        AppTab("orders", "Pedidos", Icons.AutoMirrored.Filled.List), AppTab("profile", "Perfil", Icons.Filled.Person)
    )
    fun encerrarSessao() {
        RepositorioAutenticacaoFirebase.sair(); usuarioAutenticado = false; tipoPerfilAtual = null
        navController.navigate("login") { popUpTo(navController.graph.id) { inclusive = true }; launchSingleTop = true }
    }
    fun abrirDemonstracao(tipo: TipoPerfil) {
        perfilDemonstracao = tipo; usuarioAutenticado = false; tipoPerfilAtual = null; geracaoDemonstracao++; dadosAlterados++
        navController.navigate(if (tipo == TipoPerfil.EMPRESA) "company" else "home") {
            popUpTo("escolha") { inclusive = true }; launchSingleTop = true
        }
    }
    fun adicionarAoCarrinho(produto: br.com.bebidaja.data.ProdutoCatalogo) {
        if (isBusiness) return
        val empresaNoCarrinho = estadoCarrinho.carrinho.itens.firstOrNull()?.produto?.empresaId
        if (empresaNoCarrinho != null && empresaNoCarrinho != produto.empresaId) {
            Toast.makeText(contexto, "Finalize ou limpe o carrinho antes de escolher outra empresa.", Toast.LENGTH_SHORT).show()
            return
        }
        estadoCarrinho.adicionar(ProdutoCarrinho(produto.id, produto.empresaId, produto.nome, produto.precoCentavos))
    }

    Scaffold(
        topBar = {
            if (tabs.any { it.route == currentRoute }) Row(
                Modifier.fillMaxWidth().statusBarsPadding().padding(end = 8.dp),
                horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically
            ) {
                if (demoSession != null) Text("DEMONSTRAÇÃO", color = AzulMedio, modifier = Modifier.weight(1f).padding(start = 24.dp))
                if (!isBusiness) FilledTonalIconButton(
                    onClick = { navController.navigate("cart") { launchSingleTop = true } },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = AzulClaro, contentColor = AzulEscuro)
                ) { Icon(Icons.Filled.ShoppingCart, contentDescription = "Carrinho", tint = AzulEscuro) }
            }
        },
        bottomBar = {
            if (tabs.any { it.route == currentRoute } || (!isBusiness && currentRoute == "cart")) NavigationBar(containerColor = Branco, tonalElevation = 0.dp) {
                tabs.forEach { tab -> NavigationBarItem(
                    selected = currentRoute == tab.route,
                    onClick = { navController.openTab(tab.route) },
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
                HomeScreen(listaEmpresas, listaProdutos, { navController.openTab("search") }, { id -> navController.navigate("empresa/${Uri.encode(id)}") }, ::adicionarAoCarrinho)
            }
            composable("empresa/{empresaId}") { entry ->
                val id = entry.arguments?.getString("empresaId").orEmpty()
                TelaDetalhesEmpresa(repositorio.buscarEmpresa(id), remember(id, dadosAlterados) { repositorio.listarProdutos(id, true) }, ::adicionarAoCarrinho) { navController.popBackStack() }
            }
            composable("search") { SearchScreen(listaProdutos, ::adicionarAoCarrinho) }
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
                        val result = repositorio.confirmarPedido(clienteId, estadoCarrinho.carrinho, estadoCarrinho.endereco, forma, "DEMO-BEBIDAJA-001", estadoCarrinho.chaveConfirmacao)
                        if (result.isSuccess) { estadoCarrinho.limparCarrinho(); dadosAlterados++ }
                        result
                    },
                    onPedidoCriado = { navController.navigate("orders") { popUpTo("home"); launchSingleTop = true } }
                ) else LaunchedEffect(Unit) { navController.popBackStack("cart", false) }
            }
            composable("orders") { if (!isBusiness) OrdersScreen(remember(clienteId, dadosAlterados) { repositorio.listarPorCliente(clienteId) }) }
            composable("profile") {
                val authUser = runCatching { FirebaseAuth.getInstance().currentUser }.getOrNull()
                ProfileScreen(
                    tipoPerfil = demoSession?.tipoPerfil ?: tipoPerfilAtual, demoSession = demoSession,
                    mostrarSaida = usuarioAutenticado || demoSession != null, empresa = empresaAtual,
                    nome = authUser?.displayName.orEmpty().ifBlank { nomeUsuario.ifBlank { demoSession?.nome.orEmpty() } }, email = authUser?.email.orEmpty().ifBlank { emailUsuario.ifBlank { demoSession?.email.orEmpty() } },
                    onCompanyClick = { if (isBusiness) navController.navigate("company") { launchSingleTop = true } },
                    onOrdersClick = { if (!isBusiness) navController.openTab("orders") },
                    onSignOutClick = { if (demoSession != null) sairDaDemonstracao() else encerrarSessao() }
                )
            }
            composable("company") {
                val empresa = empresaAtual
                if (isBusiness && empresa != null) CompanyScreen(
                    repositorio = repositorio, empresaInicial = empresa,
                    pedidos = remember(empresa.id, dadosAlterados) { repositorio.listarPorEmpresa(empresa.id) },
                    aoAlterarDados = { dadosAlterados++ }, onBackClick = { navController.popBackStack() },
                    onProfileClick = { navController.navigate("profile") { launchSingleTop = true } },
                    onSignOutClick = { if (demoSession != null) sairDaDemonstracao() else encerrarSessao() }
                )
            }
        }
    }
}

private fun NavHostController.openTab(route: String) {
    if (currentDestination?.route == route) return
    navigate(route) { popUpTo(if (route == "company" || currentDestination?.route == "company") "company" else "home") { inclusive = false }; launchSingleTop = true }
}
