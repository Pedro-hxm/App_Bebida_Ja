package br.com.bebidaja

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import br.com.bebidaja.ui.navigation.AppNavigation
import br.com.bebidaja.ui.theme.BebidaJaTheme

class AtividadePrincipal : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BebidaJaTheme {
                AppNavigation()
            }
        }
    }
}