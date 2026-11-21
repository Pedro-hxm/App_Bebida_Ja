package br.com.bebidaja.ui.screens

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import br.com.bebidaja.R
import br.com.bebidaja.data.PerfilIncompletoException
import br.com.bebidaja.data.RepositorioAutenticacaoFirebase
import br.com.bebidaja.data.TipoPerfil

@Composable
fun LoginScreen(
    firebaseDisponivel: Boolean,
    onRegisterClick: () -> Unit,
    onExploreClick: () -> Unit,
    onLoginSuccess: (TipoPerfil) -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var carregando by rememberSaveable { mutableStateOf(false) }
    var mensagem by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.mascote_bebida_ja),
            contentDescription = "Mascote do Bebida Já",
            modifier = Modifier.size(112.dp)
        )

        Text("Bem-vindo de volta", style = MaterialTheme.typography.headlineMedium)
        Text("Entre para encontrar bebidas perto de você.")

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("E-mail") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Senha") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Button(
            onClick = {
                val emailNormalizado = email.trim().lowercase()

                when {
                    !Patterns.EMAIL_ADDRESS.matcher(emailNormalizado).matches() -> {
                        mensagem = "Informe um e-mail válido."
                    }

                    senha.isBlank() -> {
                        mensagem = "Informe sua senha."
                    }

                    else -> {
                        carregando = true
                        mensagem = null

                        RepositorioAutenticacaoFirebase.entrar(emailNormalizado, senha) { resultado ->
                            carregando = false

                            resultado.onSuccess(onLoginSuccess)
                                .onFailure { erro ->
                                    mensagem = if (erro is PerfilIncompletoException) {
                                        "Seu cadastro está incompleto. Use Criar uma conta para concluí-lo."
                                    } else {
                                        "Não foi possível entrar. Confira os dados e tente novamente."
                                    }
                                }
                        }
                    }
                }
            },
            enabled = firebaseDisponivel && !carregando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (carregando) "Entrando..." else "Entrar")
        }

        mensagem?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        if (!firebaseDisponivel) {
            Text(
                "A autenticação será liberada após a configuração do Firebase.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        TextButton(onClick = onRegisterClick) {
            Text("Criar uma conta")
        }

        TextButton(onClick = onExploreClick) {
            Text("Explorar demonstração")
        }
    }
}

@Composable
fun RegistrationScreen(
    firebaseDisponivel: Boolean,
    onBackClick: () -> Unit,
    onRegistrationSuccess: (TipoPerfil) -> Unit
) {
    var nome by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var tipoPerfil by rememberSaveable { mutableStateOf(TipoPerfil.CLIENTE) }
    var carregando by rememberSaveable { mutableStateOf(false) }
    var mensagem by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Criar conta", style = MaterialTheme.typography.headlineMedium)
        Text("Escolha como você quer usar o Bebida Já.")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = tipoPerfil == TipoPerfil.CLIENTE,
                onClick = { tipoPerfil = TipoPerfil.CLIENTE },
                label = { Text("Cliente") }
            )
            FilterChip(
                selected = tipoPerfil == TipoPerfil.EMPRESA,
                onClick = { tipoPerfil = TipoPerfil.EMPRESA },
                label = { Text("Empresa") }
            )
        }

        OutlinedTextField(
            value = nome,
            onValueChange = { if (it.length <= 80) nome = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(if (tipoPerfil == TipoPerfil.EMPRESA) "Nome da empresa" else "Seu nome") },
            singleLine = true
        )

        OutlinedTextField(
            value = email,
            onValueChange = { if (it.length <= 254) email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("E-mail") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Senha") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Button(
            onClick = {
                val nomeNormalizado = nome.trim()
                val emailNormalizado = email.trim().lowercase()

                when {
                    nomeNormalizado.isBlank() -> {
                        mensagem = "Informe seu nome ou o nome da empresa."
                    }

                    !Patterns.EMAIL_ADDRESS.matcher(emailNormalizado).matches() -> {
                        mensagem = "Informe um e-mail válido."
                    }

                    senha.length < 6 -> {
                        mensagem = "A senha precisa ter pelo menos 6 caracteres."
                    }

                    else -> {
                        carregando = true
                        mensagem = null

                        RepositorioAutenticacaoFirebase.criarConta(
                            nomeNormalizado,
                            emailNormalizado,
                            senha,
                            tipoPerfil
                        ) { resultado ->
                            carregando = false

                            resultado.onSuccess(onRegistrationSuccess)
                                .onFailure {
                                    mensagem = "Não foi possível criar a conta. Confira os dados e tente novamente."
                                }
                        }
                    }
                }
            },
            enabled = firebaseDisponivel && !carregando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (carregando) "Criando conta..." else "Criar conta")
        }

        mensagem?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        if (!firebaseDisponivel) {
            Text(
                "O cadastro será liberado após a configuração do Firebase.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        TextButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Voltar ao login")
        }
    }
}
