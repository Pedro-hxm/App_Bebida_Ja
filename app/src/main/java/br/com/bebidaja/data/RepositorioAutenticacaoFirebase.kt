package br.com.bebidaja.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

enum class TipoPerfil(val valorFirestore: String) {
    CLIENTE("cliente"),
    EMPRESA("empresa")
}

class PerfilIncompletoException : Exception()

object RepositorioAutenticacaoFirebase {
    private val autenticacao: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    private val bancoDeDados: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    fun entrar(
        email: String,
        senha: String,
        aoConcluir: (Result<TipoPerfil>) -> Unit
    ) {
        autenticacao.signInWithEmailAndPassword(email, senha)
            .addOnSuccessListener { resultado ->
                val usuario = resultado.user

                if (usuario == null) {
                    aoConcluir(Result.failure(IllegalStateException()))
                    return@addOnSuccessListener
                }

                bancoDeDados.collection("usuarios")
                    .document(usuario.uid)
                    .get()
                    .addOnSuccessListener { documento ->
                        val tipoPerfil = TipoPerfil.entries.firstOrNull {
                            it.valorFirestore == documento.getString("tipoPerfil")
                        }

                        if (documento.exists() && tipoPerfil != null) {
                            aoConcluir(Result.success(tipoPerfil))
                        } else {
                            aoConcluir(Result.failure(PerfilIncompletoException()))
                        }
                    }
                    .addOnFailureListener { erro ->
                        aoConcluir(Result.failure(erro))
                    }
            }
            .addOnFailureListener { erro ->
                aoConcluir(Result.failure(erro))
            }
    }

    fun criarConta(
        nome: String,
        email: String,
        senha: String,
        tipoPerfil: TipoPerfil,
        aoConcluir: (Result<TipoPerfil>) -> Unit
    ) {
        val usuarioAtual = autenticacao.currentUser

        if (usuarioAtual != null && usuarioAtual.email.equals(email, ignoreCase = true)) {
            salvarPerfil(usuarioAtual, nome, tipoPerfil, aoConcluir)
            return
        }

        autenticacao.createUserWithEmailAndPassword(email, senha)
            .addOnSuccessListener { resultado ->
                val usuario = resultado.user

                if (usuario == null) {
                    aoConcluir(Result.failure(IllegalStateException()))
                    return@addOnSuccessListener
                }

                salvarPerfil(usuario, nome, tipoPerfil, aoConcluir)
            }
            .addOnFailureListener { erro ->
                aoConcluir(Result.failure(erro))
            }
    }

    fun sair() {
        autenticacao.signOut()
    }

    private fun salvarPerfil(
        usuario: com.google.firebase.auth.FirebaseUser,
        nome: String,
        tipoPerfil: TipoPerfil,
        aoConcluir: (Result<TipoPerfil>) -> Unit
    ) {
        val dados = hashMapOf(
            "uid" to usuario.uid,
            "nome" to nome.trim(),
            "email" to (usuario.email ?: ""),
            "tipoPerfil" to tipoPerfil.valorFirestore
        )

        bancoDeDados.collection("usuarios")
            .document(usuario.uid)
            .set(dados)
            .addOnSuccessListener {
                aoConcluir(Result.success(tipoPerfil))
            }
            .addOnFailureListener { erro ->
                aoConcluir(Result.failure(erro))
            }
    }
}
