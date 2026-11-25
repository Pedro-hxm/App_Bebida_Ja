package br.com.bebidaja.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.bebidaja.data.RepositorioAutenticacaoFirebase
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

    val tabs = listOf(
        AppTab("home", "Início", Icons.Filled.Home),
        AppTab("search", "Buscar", Icons.Filled.Search),
        AppTab("cart", "Carrinho", Icons.Filled.ShoppingCart),
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
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = { navController.openTab(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
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
            composable("cart") { TelaCarrinho(estadoCarrinho) { } }
            composable("orders") { OrdersScreen() }
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
