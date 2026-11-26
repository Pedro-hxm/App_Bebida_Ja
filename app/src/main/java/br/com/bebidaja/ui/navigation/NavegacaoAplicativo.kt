package br.com.bebidaja.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.bebidaja.data.RepositorioAutenticacaoFirebase
import br.com.bebidaja.data.RepositorioPedidosEmMemoria
import br.com.bebidaja.data.EnderecoEntrega
import br.com.bebidaja.data.FormaPagamento
import br.com.bebidaja.data.ProdutoCarrinho
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
import br.com.bebidaja.ui.screens.TelaEnderecoEntrega
import br.com.bebidaja.ui.screens.TelaCheckout
import br.com.bebidaja.ui.screens.TelaComprovante
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
    val estadoCarrinho = remember { EstadoCarrinhoUi() }
    val repositorioPedidos = remember { RepositorioPedidosEmMemoria() }
    val clienteIdAtual = remember(usuarioAutenticado) {
        runCatching { FirebaseAuth.getInstance().currentUser?.uid }.getOrNull()
            ?: "cliente-demonstracao"
    }

    val tabs = listOf(
        AppTab("home", "Início", Icons.Filled.Home),
        AppTab("search", "Buscar", Icons.Filled.Search),
        AppTab("cart", "Carrinho", Icons.Filled.ShoppingCart),
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

    fun abrirInicioDoPerfil(tipoPerfil: TipoPerfil) {
        usuarioAutenticado = true
        tipoPerfilAtual = tipoPerfil

        navController.navigate("home") {
            popUpTo("login") { inclusive = true }
            launchSingleTop = true
        }

        if (tipoPerfil == TipoPerfil.EMPRESA) {
            navController.navigate("company") {
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (tabs.any { it.route == currentRoute }) {
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
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            composable("login") {
                LoginScreen(
                    firebaseDisponivel = firebaseDisponivel,
                    onRegisterClick = { navController.navigate("cadastro") },
                    onExploreClick = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
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
                    onSearchClick = { navController.openTab("search") },
                    onAddToCart = { bebida ->
                        val centavos = bebida.price.filter(Char::isDigit).toLongOrNull() ?: 0L
                        estadoCarrinho.adicionar(
                            ProdutoCarrinho(
                                produtoId = bebida.name.lowercase().replace(" ", "-"),
                                empresaId = "demonstracao",
                                nome = bebida.name,
                                precoUnitarioCentavos = centavos
                            )
                        )
                    }
                )
            }
            composable("search") { SearchScreen() }
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
                    navController.popBackStack("cart", inclusive = false)
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
                    navController.popBackStack("cart", inclusive = false)
                }
            }
            composable("orders") {
                OrdersScreen(repositorioPedidos.listarPorCliente(clienteIdAtual))
            }
            composable("profile") {
                ProfileScreen(
                    tipoPerfil = tipoPerfilAtual,
                    mostrarSaida = usuarioAutenticado,
                    onCompanyClick = { navController.navigate("company") },
                    onSignOutClick = ::encerrarSessao
                )
            }
            composable("company") {
                CompanyScreen(
                    onBackClick = { navController.popBackStack() },
                    mostrarSaida = usuarioAutenticado,
                    onSignOutClick = ::encerrarSessao
                )
            }
        }
    }
}

private fun NavHostController.openTab(route: String) {
    navigate(route) {
        popUpTo("home")
        launchSingleTop = true
    }
}
