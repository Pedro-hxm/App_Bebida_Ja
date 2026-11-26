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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import br.com.bebidaja.R
import br.com.bebidaja.data.DadosComerciaisEmpresa
import br.com.bebidaja.data.PerfilIncompletoException
import br.com.bebidaja.data.RepositorioAutenticacaoFirebase
import br.com.bebidaja.data.TipoPerfil
import br.com.bebidaja.ui.components.SeletorImagemLocal

private class TelaDeAcessoAtiva(var ativa: Boolean = true)

@Composable
fun LoginScreen(firebaseDisponivel: Boolean, onRegisterClick: () -> Unit, onExploreClick: () -> Unit, onLoginSuccess: (TipoPerfil) -> Unit) {
    val tela = remember { TelaDeAcessoAtiva() }
    DisposableEffect(tela) { tela.ativa = true; onDispose { tela.ativa = false } }
    var email by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var carregando by rememberSaveable { mutableStateOf(false) }
    var mensagem by rememberSaveable { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Image(painterResource(R.drawable.mascote_bebida_ja), "Mascote do Bebida Já", Modifier.size(112.dp))
        Text("Bem-vindo de volta", style = MaterialTheme.typography.headlineMedium)
        Text("Entre para encontrar bebidas perto de você.")
        OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("E-mail") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
        OutlinedTextField(senha, { senha = it }, Modifier.fillMaxWidth(), label = { Text("Senha") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
        Button(onClick = {
            val normalizado = email.trim().lowercase()
            when {
                !Patterns.EMAIL_ADDRESS.matcher(normalizado).matches() -> mensagem = "Informe um e-mail válido."
                senha.isBlank() -> mensagem = "Informe sua senha."
                else -> { carregando = true; mensagem = null; RepositorioAutenticacaoFirebase.entrar(normalizado, senha) { result ->
                    if (!tela.ativa) return@entrar
                    carregando = false; result.onSuccess(onLoginSuccess).onFailure { erro ->
                        mensagem = if (erro is PerfilIncompletoException) "Seu cadastro está incompleto. Crie uma conta para concluí-lo." else "Não foi possível entrar. Confira os dados e tente novamente."
                    }
                } }
            }
        }, enabled = firebaseDisponivel && !carregando, modifier = Modifier.fillMaxWidth()) { Text(if (carregando) "Entrando…" else "Entrar") }
        mensagem?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (!firebaseDisponivel) Text("Configure o Firebase para habilitar contas reais.", style = MaterialTheme.typography.bodySmall)
        TextButton(onRegisterClick) { Text("Criar uma conta") }
        TextButton(onExploreClick) { Text("Explorar demonstração") }
    }
}

@Composable
fun RegistrationScreen(
    firebaseDisponivel: Boolean,
    onBackClick: () -> Unit,
    onRegistrationSuccess: (TipoPerfil, String, DadosComerciaisEmpresa?) -> Unit
) {
    val tela = remember { TelaDeAcessoAtiva() }
    DisposableEffect(tela) { tela.ativa = true; onDispose { tela.ativa = false } }
    var nome by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var endereco by rememberSaveable { mutableStateOf("") }
    var telefone by rememberSaveable { mutableStateOf("") }
    var horario by rememberSaveable { mutableStateOf("") }
    var area by rememberSaveable { mutableStateOf("") }
    var regiao by rememberSaveable { mutableStateOf("Goiana") }
    var imagemEmpresa by rememberSaveable { mutableStateOf<String?>(null) }
    var tipo by rememberSaveable { mutableStateOf(TipoPerfil.CLIENTE) }
    var carregando by rememberSaveable { mutableStateOf(false) }
    var mensagem by rememberSaveable { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Criar conta", style = MaterialTheme.typography.headlineMedium)
        Text("Escolha o perfil e informe os dados da conta.")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(tipo == TipoPerfil.CLIENTE, { tipo = TipoPerfil.CLIENTE }, label = { Text("Cliente") })
            FilterChip(tipo == TipoPerfil.EMPRESA, { tipo = TipoPerfil.EMPRESA }, label = { Text("Empresa") })
        }
        OutlinedTextField(nome, { if (it.length <= 100) nome = it }, Modifier.fillMaxWidth(), label = { Text(if (tipo == TipoPerfil.EMPRESA) "Nome comercial" else "Seu nome") }, singleLine = true)
        OutlinedTextField(email, { if (it.length <= 254) email = it }, Modifier.fillMaxWidth(), label = { Text("E-mail") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
        OutlinedTextField(senha, { senha = it }, Modifier.fillMaxWidth(), label = { Text("Senha (mínimo 6 caracteres)") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
        if (tipo == TipoPerfil.EMPRESA) {
            Text("Cadastro comercial", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(endereco, { endereco = it }, Modifier.fillMaxWidth(), label = { Text("Endereço") }, singleLine = true)
            OutlinedTextField(telefone, { if (it.length <= 20) telefone = it }, Modifier.fillMaxWidth(), label = { Text("Telefone com DDD") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            OutlinedTextField(horario, { horario = it }, Modifier.fillMaxWidth(), label = { Text("Horário de funcionamento") }, singleLine = true)
            OutlinedTextField(area, { area = it }, Modifier.fillMaxWidth(), label = { Text("Área de entrega") }, singleLine = true)
            OutlinedTextField(regiao, { if (it.length <= 80) regiao = it }, Modifier.fillMaxWidth(), label = { Text("Região") }, singleLine = true)
            SeletorImagemLocal("Imagem da empresa (opcional)", "empresas", imagemEmpresa) { imagemEmpresa = it }
        }
        Button(onClick = {
            val n = nome.trim(); val e = email.trim().lowercase()
            val comercial = if (tipo == TipoPerfil.EMPRESA) DadosComerciaisEmpresa(n, endereco.trim(), telefone.trim(), horario.trim(), area.trim(), regiao.trim(), imagemEmpresa) else null
            when {
                n.isBlank() -> mensagem = "Informe seu nome ou o nome comercial."
                !Patterns.EMAIL_ADDRESS.matcher(e).matches() -> mensagem = "Informe um e-mail válido."
                senha.length < 6 -> mensagem = "A senha precisa ter pelo menos 6 caracteres."
                comercial != null && comercial.endereco.isBlank() -> mensagem = "Informe o endereço da empresa."
                comercial != null && comercial.telefone.filter(Char::isDigit).length < 10 -> mensagem = "Informe um telefone com DDD."
                comercial != null && comercial.horario.isBlank() -> mensagem = "Informe o horário de funcionamento."
                comercial != null && comercial.areaEntrega.isBlank() -> mensagem = "Informe a área de entrega."
                comercial != null && comercial.regiao.isBlank() -> mensagem = "Informe a região de atendimento."
                else -> {
                    carregando = true; mensagem = null
                    RepositorioAutenticacaoFirebase.criarConta(n, e, senha, tipo) { resultado ->
                        if (!tela.ativa) return@criarConta
                        carregando = false
                        resultado.onSuccess { perfil -> onRegistrationSuccess(perfil, n, comercial) }.onFailure {
                            mensagem = "Não foi possível criar a conta. Confira os dados e tente novamente."
                        }
                    }
                }
            }
        }, enabled = firebaseDisponivel && !carregando, modifier = Modifier.fillMaxWidth()) { Text(if (carregando) "Criando conta…" else "Criar conta") }
        mensagem?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (!firebaseDisponivel) Text("Configure o Firebase para habilitar contas reais.", style = MaterialTheme.typography.bodySmall)
        TextButton(onBackClick, Modifier.align(Alignment.CenterHorizontally)) { Text("Voltar ao login") }
    }
}
