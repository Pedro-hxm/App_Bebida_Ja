package br.com.bebidaja.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
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

@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit,
    onExploreClick: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }

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

        Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
            Text("Entrar")
        }

        Text(
            "O acesso por conta estará disponível em breve.",
            style = MaterialTheme.typography.bodySmall
        )

        TextButton(onClick = onRegisterClick) {
            Text("Criar uma conta")
        }

        TextButton(onClick = onExploreClick) {
            Text("Explorar demonstração")
        }
    }
}

@Composable
fun RegistrationScreen(onBackClick: () -> Unit) {
    var nome by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var tipoPerfil by rememberSaveable { mutableStateOf("cliente") }

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
                selected = tipoPerfil == "cliente",
                onClick = { tipoPerfil = "cliente" },
                label = { Text("Cliente") }
            )
            FilterChip(
                selected = tipoPerfil == "empresa",
                onClick = { tipoPerfil = "empresa" },
                label = { Text("Empresa") }
            )
        }

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(if (tipoPerfil == "empresa") "Nome da empresa" else "Seu nome") },
            singleLine = true
        )

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

        Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
            Text("Criar conta")
        }

        Text(
            "O cadastro por conta estará disponível em breve.",
            style = MaterialTheme.typography.bodySmall
        )

        TextButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Voltar ao login")
        }
    }
}
