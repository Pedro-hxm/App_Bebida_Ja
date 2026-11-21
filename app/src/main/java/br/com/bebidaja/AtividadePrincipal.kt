package br.com.bebidaja

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import br.com.bebidaja.ui.navigation.NavegacaoAplicativo
import br.com.bebidaja.ui.theme.BebidaJaTheme
import com.google.firebase.FirebaseApp

class AtividadePrincipal : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val firebaseDisponivel = FirebaseApp.initializeApp(this) != null

        setContent {
            BebidaJaTheme {
                NavegacaoAplicativo(firebaseDisponivel)
            }
        }
    }
}
