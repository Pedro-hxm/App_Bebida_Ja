package br.com.bebidaja.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.List
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.bebidaja.data.RepositorioAutenticacaoFirebase
import br.com.bebidaja.data.RepositorioCatalogoDemonstrativo
import br.com.bebidaja.data.RepositorioPedidosEmMemoria
import br.com.bebidaja.data.EnderecoEntrega
import br.com.bebidaja.data.FormaPagamento
import br.com.bebidaja.data.ProdutoCarrinho
import br.com.bebidaja.data.ProdutoDemonstrativo
import br.com.bebidaja.data.TipoPerfil
import br.com.bebidaja.data.demoStores
import br.com.bebidaja.data.findDemoStoreById
import br.com.bebidaja.data.mapearProdutoParaCarrinho
import br.com.bebidaja.data.podeAdicionarAoCarrinho
import br.com.bebidaja.ui.screens.CompanyScreen
import br.com.bebidaja.ui.screens.HomeScreen
import br.com.bebidaja.ui.screens.TelaCatalogoEmpresa
import br.com.bebidaja.ui.screens.TelaDetalhesEmpresa
import br.com.bebidaja.ui.screens.TelaDetalhesProduto
import br.com.bebidaja.ui.screens.TelaGerenciamentoProdutos
import br.com.bebidaja.ui.screens.TelaPesquisaCatalogo
import br.com.bebidaja.ui.screens.LoginScreen
import br.com.bebidaja.ui.screens.OrdersScreen
import br.com.bebidaja.ui.screens.ProfileScreen
import br.com.bebidaja.ui.screens.RegistrationScreen
import br.com.bebidaja.ui.screens.SplashScreen
import br.com.bebidaja.ui.screens.EstadoCarrinhoUi
import br.com.bebidaja.ui.screens.TelaCarrinho
import br.com.bebidaja.ui.screens.TelaEnderecoEntrega
import br.com.bebidaja.ui.screens.TelaCheckout
import br.com.bebidaja.ui.screens.TelaComprovante
import br.com.bebidaja.ui.screens.TelaEscolhaDemonstracao
import br.com.bebidaja.ui.theme.AzulClaro
import br.com.bebidaja.ui.theme.AzulEscuro
import br.com.bebidaja.ui.theme.AzulMedio
import br.com.bebidaja.ui.theme.Branco

private data class AppTab(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun NavegacaoAplicativo(firebaseDisponivel: Boolean) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    var usuarioAutenticado by rememberSaveable { mutableStateOf(false) }
    var tipoPerfilAtual by rememberSaveable { mutableStateOf<TipoPerfil?>(null) }
    var perfilDemonstracao by rememberSaveable { mutableStateOf<TipoPerfil?>(null) }
    var geracaoDemonstracao by rememberSaveable { mutableStateOf(0) }
    val demoSession = perfilDemonstracao?.let(::criarDemoSession)
    val estadoCarrinho = remember(geracaoDemonstracao) { EstadoCarrinhoUi() }
    val repositorioPedidos = remember(geracaoDemonstracao) { RepositorioPedidosEmMemoria() }
    val repositorioCatalogo = remember { RepositorioCatalogoDemonstrativo() }
    var produtosCatalogo by remember {
        mutableStateOf(repositorioCatalogo.listarTodos())
    }
    val empresaIdGerenciada = when {
        demoSession?.tipoPerfil == TipoPerfil.EMPRESA -> "adega-central"
        usuarioAutenticado && tipoPerfilAtual == TipoPerfil.EMPRESA ->
            runCatching { FirebaseAuth.getInstance().currentUser?.uid }.getOrNull()
        else -> null
    }
    val podeGerenciarProdutos = empresaIdGerenciada != null &&
        (demoSession?.tipoPerfil == TipoPerfil.EMPRESA ||
            usuarioAutenticado && tipoPerfilAtual == TipoPerfil.EMPRESA)
    val clienteIdAtual = remember(usuarioAutenticado, perfilDemonstracao) {
        if (usuarioAutenticado && perfilDemonstracao == null) {
            runCatching { FirebaseAuth.getInstance().currentUser?.uid }.getOrNull()
                ?: "cliente-demonstracao"
        } else {
            "cliente-demonstracao"
        }
    }

    fun atualizarCatalogo() {
        produtosCatalogo = repositorioCatalogo.listarTodos()
    }

    fun adicionarAoCarrinho(produto: ProdutoDemonstrativo): Boolean {
        if (!podeAdicionarAoCarrinho(produto, estadoCarrinho.carrinho)) return false
        val item = mapearProdutoParaCarrinho(produto) ?: return false
        estadoCarrinho.adicionar(item)
        return true
    }

    val tabs = listOf(
        AppTab("home", "Início", Icons.Filled.Home),
        AppTab("search", "Pesquisa", Icons.Filled.Search),
        AppTab("orders", "Pedidos", Icons.AutoMirrored.Filled.List),
        AppTab("profile", "Perfil", Icons.Filled.Person)
    )

    fun encerrarSessao() {
        RepositorioAutenticacaoFirebase.sair()
        usuarioAutenticado = false
        tipoPerfilAtual = null
        navController.navigate("login") {
            popUpTo("home") { inclusive = true }
            launchSingleTop = true
        }
    }

    fun sairDaDemonstracao() {
        perfilDemonstracao = null
        geracaoDemonstracao++
        navController.navigate("escolha") {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun abrirDemonstracao(tipoPerfil: TipoPerfil) {
        perfilDemonstracao = tipoPerfil
        usuarioAutenticado = false
        tipoPerfilAtual = null
        geracaoDemonstracao++
        navController.navigate("home") {
            popUpTo("escolha") { inclusive = true }
            launchSingleTop = true
        }
        if (tipoPerfil == TipoPerfil.EMPRESA) {
            navController.navigate("company") { launchSingleTop = true }
        }
    }

    fun abrirInicioDoPerfil(tipoPerfil: TipoPerfil) {
        perfilDemonstracao = null
        usuarioAutenticado = true
        tipoPerfilAtual = tipoPerfil

        navController.navigate("home") {
            popUpTo("escolha") { inclusive = true }
            launchSingleTop = true
        }

        if (tipoPerfil == TipoPerfil.EMPRESA) {
            navController.navigate("company") {
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        topBar = {
            if (tabs.any { it.route == currentRoute }) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(end = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (demoSession != null) {
                        Text("DEMONSTRAÇÃO", style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = AzulMedio, modifier = Modifier.weight(1f).padding(start = 24.dp))
                    }
                    FilledTonalIconButton(
                        onClick = { navController.navigate("cart") { launchSingleTop = true } },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = AzulClaro,
                            contentColor = AzulEscuro
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = "Carrinho",
                            tint = AzulEscuro
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (tabs.any { it.route == currentRoute } || currentRoute == "cart") {
                NavigationBar(containerColor = Branco, tonalElevation = 0.dp) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = { navController.openTab(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AzulEscuro,
                                selectedTextColor = AzulEscuro,
                                indicatorColor = AzulClaro,
                                unselectedIconColor = AzulMedio,
                                unselectedTextColor = AzulMedio
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("splash") {
                SplashScreen {
                    navController.navigate("escolha") {
                        popUpTo("splash") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            composable("escolha") {
                TelaEscolhaDemonstracao(
                    onEscolher = ::abrirDemonstracao,
                    onAcessoReal = { navController.navigate("login") { launchSingleTop = true } }
                )
            }
            composable("login") {
                LoginScreen(
                    firebaseDisponivel = firebaseDisponivel,
                    onRegisterClick = { navController.navigate("cadastro") },
                    onExploreClick = {
                        navController.navigate("escolha") {
                            popUpTo("login") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onLoginSuccess = ::abrirInicioDoPerfil
                )
            }
            composable("cadastro") {
                RegistrationScreen(
                    firebaseDisponivel = firebaseDisponivel,
                    onBackClick = { navController.popBackStack() },
                    onRegistrationSuccess = ::abrirInicioDoPerfil
                )
            }
            composable("home") {
                HomeScreen(
                    produtos = produtosCatalogo,
                    onSearchClick = { navController.openTab("search") },
                    onStoreClick = { storeId ->
                        navController.navigate("empresa/${Uri.encode(storeId)}")
                    },
                    onProductClick = { productId ->
                        navController.navigate("produto-detalhe/${Uri.encode(productId)}")
                    },
                    onAddToCart = ::adicionarAoCarrinho
                )
            }
            composable("empresa/{empresaId}") { backStackEntry ->
                val empresa = backStackEntry.arguments?.getString("empresaId")
                    ?.let { findDemoStoreById(it) }
                TelaDetalhesEmpresa(
                    empresa = empresa,
                    onVoltar = { navController.popBackStack() },
                    onAbrirCatalogo = {
                        if (empresa != null) {
                            navController.navigate(
                                "empresa/${Uri.encode(empresa.id)}/catalogo"
                            )
                        }
                    }
                )
            }
            composable("empresa/{empresaId}/catalogo") { backStackEntry ->
                val empresaId = backStackEntry.arguments?.getString("empresaId").orEmpty()
                val empresa = findDemoStoreById(empresaId)
                TelaCatalogoEmpresa(
                    empresa = empresa,
                    produtos = produtosCatalogo.filter { it.empresaId == empresaId },
                    onVoltar = { navController.popBackStack() },
                    onProdutoClick = { productId ->
                        navController.navigate("produto-detalhe/${Uri.encode(productId)}")
                    },
                    onAdicionarAoCarrinho = ::adicionarAoCarrinho
                )
            }
            composable("produto-detalhe/{produtoId}") { backStackEntry ->
                val produtoId = backStackEntry.arguments?.getString("produtoId").orEmpty()
                val produto = produtosCatalogo.firstOrNull { it.id == produtoId }
                val empresa = produto?.let { findDemoStoreById(it.empresaId) }
                TelaDetalhesProduto(
                    produto = produto,
                    empresa = empresa,
                    onVoltar = { navController.popBackStack() },
                    onAdicionarAoCarrinho = ::adicionarAoCarrinho
                )
            }
            composable("empresa/{empresaId}/produtos") { backStackEntry ->
                val empresaId = backStackEntry.arguments?.getString("empresaId").orEmpty()
                if (podeGerenciarProdutos && empresaId == empresaIdGerenciada) {
                    TelaGerenciamentoProdutos(
                        empresaId = empresaId,
                        empresa = findDemoStoreById(empresaId),
                        nomeEmpresa = findDemoStoreById(empresaId)?.name
                            ?: "Empresa demonstrativa",
                        produtos = produtosCatalogo.filter { it.empresaId == empresaId },
                        onVoltar = { navController.popBackStack() },
                        onSalvarProduto = { produto ->
                            val salvo = repositorioCatalogo.salvar(produto)
                            if (salvo) atualizarCatalogo()
                            salvo
                        },
                        onAlterarDisponibilidade = { productId, companyId, disponivel ->
                            val alterado = repositorioCatalogo.alterarDisponibilidade(
                                productId,
                                companyId,
                                disponivel
                            )
                            if (alterado) atualizarCatalogo()
                            alterado
                        },
                        onRemoverProduto = { productId, companyId ->
                            val removido = repositorioCatalogo.remover(productId, companyId)
                            if (removido) atualizarCatalogo()
                            removido
                        }
                    )
                } else {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Este catálogo não está disponível para o perfil atual.")
                        androidx.compose.material3.Button(
                            onClick = { navController.popBackStack() }
                        ) {
                            Text("Voltar")
                        }
                    }
                }
            }
            composable("search") {
                TelaPesquisaCatalogo(
                    produtos = produtosCatalogo,
                    empresas = demoStores,
                    onEmpresaClick = { storeId ->
                        navController.navigate("empresa/${Uri.encode(storeId)}")
                    },
                    onProdutoClick = { productId ->
                        navController.navigate("produto-detalhe/${Uri.encode(productId)}")
                    },
                    onAdicionarAoCarrinho = ::adicionarAoCarrinho
                )
            }
            composable("cart") {
                TelaCarrinho(estadoCarrinho) {
                    if (!estadoCarrinho.carrinho.estaVazio) {
                        navController.navigate("endereco")
                    }
                }
            }
            composable("endereco") {
                TelaEnderecoEntrega(
                    enderecoInicial = estadoCarrinho.endereco,
                    onSalvar = { endereco: EnderecoEntrega ->
                        estadoCarrinho.salvarEndereco(endereco)
                    },
                    onContinuar = { navController.navigate("checkout") }
                )
            }
            composable("checkout") {
                val endereco = estadoCarrinho.endereco
                if (endereco != null && !estadoCarrinho.carrinho.estaVazio) {
                    TelaCheckout(
                        itens = estadoCarrinho.carrinho.itens,
                        endereco = endereco,
                        pagamentoInicial = estadoCarrinho.formaPagamento,
                        totalCentavos = estadoCarrinho.carrinho.totalCentavos,
                        onConfirmar = { forma: FormaPagamento ->
                            estadoCarrinho.selecionarFormaPagamento(forma)
                        },
                        onIrParaComprovante = { navController.navigate("comprovante") }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.popBackStack("cart", inclusive = false)
                    }
                }
            }
            composable("comprovante") {
                val forma = estadoCarrinho.formaPagamento
                if (forma != null && !estadoCarrinho.carrinho.estaVazio) {
                    TelaComprovante(
                        formaPagamento = forma,
                        totalCentavos = estadoCarrinho.carrinho.totalCentavos,
                        onCriarPedido = {
                            val resultado = repositorioPedidos.confirmarPedido(
                                clienteId = clienteIdAtual,
                                carrinho = estadoCarrinho.carrinho,
                                endereco = estadoCarrinho.endereco,
                                formaPagamento = forma,
                                referenciaComprovante = "DEMO-BEBIDAJA-001",
                                chaveConfirmacao = estadoCarrinho.chaveConfirmacao
                            )
                            if (resultado.isSuccess) estadoCarrinho.limparCarrinho()
                            resultado
                        },
                        onPedidoCriado = {
                            navController.navigate("orders") {
                                popUpTo("home")
                                launchSingleTop = true
                            }
                        }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.popBackStack("cart", inclusive = false)
                    }
                }
            }
            composable("orders") {
                OrdersScreen(repositorioPedidos.listarPorCliente(clienteIdAtual))
            }
            composable("profile") {
                ProfileScreen(
                    tipoPerfil = demoSession?.tipoPerfil ?: tipoPerfilAtual,
                    demoSession = demoSession,
                    mostrarSaida = usuarioAutenticado || demoSession != null,
                    onCompanyClick = { navController.navigate("company") },
                    onOrdersClick = { navController.openTab("orders") },
                    onSignOutClick = { if (demoSession != null) sairDaDemonstracao() else encerrarSessao() }
                )
            }
            composable("company") {
                CompanyScreen(
                    onBackClick = { navController.popBackStack() },
                    demoSession = demoSession,
                    mostrarSaida = usuarioAutenticado || demoSession != null,
                    podeGerenciarProdutos = podeGerenciarProdutos,
                    onManageProductsClick = {
                        empresaIdGerenciada?.let { empresaId ->
                            navController.navigate(
                                "empresa/${Uri.encode(empresaId)}/produtos"
                            )
                        }
                    },
                    onProfileClick = { navController.navigate("profile") { launchSingleTop = true } },
                    onSignOutClick = { if (demoSession != null) sairDaDemonstracao() else encerrarSessao() }
                )
            }
        }
    }
}

private fun NavHostController.openTab(route: String) {
    if (currentDestination?.route == route) return
    navigate(route) {
        popUpTo("home")
        launchSingleTop = true
    }
}
