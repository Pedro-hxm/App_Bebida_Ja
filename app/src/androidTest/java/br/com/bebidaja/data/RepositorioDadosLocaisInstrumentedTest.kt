package br.com.bebidaja.data

import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepositorioDadosLocaisInstrumentedTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun limparBanco() {
        context.deleteDatabase("bebidaja.db")
        File(context.filesDir, "imagens").deleteRecursively()
    }

    @After
    fun limparDepois() {
        context.deleteDatabase("bebidaja.db")
        File(context.filesDir, "imagens").deleteRecursively()
    }

    @Test
    fun empresaRegiaoEImagensDeEmpresaEProdutoSobrevivemAoReabrirBanco() {
        val repo = RepositorioDadosLocais(context)
        val companyImage = criarArquivoImagem("empresas/logo.jpg")
        val productImage = criarArquivoImagem("produtos/bebida.jpg")
        val empresa = Empresa("empresa-local-1", "usuario-local-1", "Distribuidora Central", "central@example.com", "Rua A", "81999991000", "8h–22h", "Goiana", "Olinda", companyImage)
        assertTrue(repo.salvarEmpresa(empresa))
        val produto = ProdutoCatalogo("produto-local-1", empresa.id, "Água", "Mineral", "Água", 500, volume = "1,5 L", imagemPath = productImage)
        assertTrue(repo.salvarProduto(produto))
        repo.fechar()

        val reaberto = RepositorioDadosLocais(context)
        assertEquals("Olinda", reaberto.buscarEmpresa(empresa.id)?.regiao)
        assertEquals(companyImage, reaberto.buscarEmpresa(empresa.id)?.imagemPath)
        assertEquals(productImage, reaberto.listarProdutos(empresa.id).single().imagemPath)
        reaberto.fechar()
    }

    @Test
    fun empresaNaoPodeSobrescreverOutraContaEProdutoFicaIsoladoPelaEmpresa() {
        val repo = RepositorioDadosLocais(context)
        val empresa = empresaTeste("empresa-1", "usuario-1")
        assertTrue(repo.salvarEmpresa(empresa))
        assertFalse(repo.salvarEmpresa(empresa.copy(usuarioId = "usuario-2", nome = "Conta invasora")))
        assertFalse(repo.salvarProduto(ProdutoCatalogo("produto-alheio", "empresa-inexistente", "Água", "Mineral", "Água", 500)))
        assertFalse(repo.definirDisponibilidade("agua-adega", "empresa-1", false))
        assertEquals("Distribuidora", repo.buscarEmpresa(empresa.id)?.nome)
        repo.fechar()
    }

    @Test
    fun pedidoPersisteEStatusSoAvancaPelaEmpresaDona() {
        val repo = RepositorioDadosLocais(context)
        val empresa = Empresa("empresa-order", "usuario-order", "Adega", "a@example.com", "Centro", "81999991001", "8h-22h", "Goiana", "Goiana")
        assertTrue(repo.salvarEmpresa(empresa))
        val cart = Carrinho().apply { adicionar(ProdutoCarrinho("produto-order", empresa.id, "Água", 450)) }
        val pedido = repo.confirmarPedido("cliente-1", cart, EnderecoEntrega("Rua A", "10", "Centro", "Goiana", "55900000"), FormaPagamento.PIX, "DEMO-TESTE", "chave-teste", 123L).getOrThrow()
        repo.fechar()

        val reaberto = RepositorioDadosLocais(context)
        assertEquals(pedido.id, reaberto.listarPorCliente("cliente-1").single().id)
        assertFalse(reaberto.atualizarStatus(pedido.id, "outra-empresa", StatusPedido.EM_PREPARACAO))
        assertTrue(reaberto.atualizarStatus(pedido.id, empresa.id, StatusPedido.EM_PREPARACAO))
        assertFalse(reaberto.atualizarStatus(pedido.id, empresa.id, StatusPedido.ENTREGUE))
        assertEquals(StatusPedido.EM_PREPARACAO, reaberto.listarPorEmpresa(empresa.id).single().status)
        reaberto.fechar()
    }

    @Test
    fun bancoVersaoDoisMigraAsColunasDeImagemSemApagarOsDadosComerciais() {
        val database = SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath("bebidaja.db"), null)
        database.execSQL("CREATE TABLE empresas (id TEXT PRIMARY KEY, usuario_id TEXT NOT NULL UNIQUE, nome TEXT NOT NULL, email TEXT NOT NULL, endereco TEXT NOT NULL, telefone TEXT NOT NULL, horario TEXT NOT NULL, area_entrega TEXT NOT NULL, regiao TEXT NOT NULL, ativa INTEGER NOT NULL DEFAULT 1)")
        database.execSQL("CREATE TABLE produtos (id TEXT PRIMARY KEY, empresa_id TEXT NOT NULL, nome TEXT NOT NULL, descricao TEXT NOT NULL, volume TEXT NOT NULL DEFAULT '', categoria TEXT NOT NULL, preco_centavos INTEGER NOT NULL, disponivel INTEGER NOT NULL DEFAULT 1, imagem_res INTEGER)")
        database.execSQL("INSERT INTO empresas(id, usuario_id, nome, email, endereco, telefone, horario, area_entrega, regiao) VALUES('legada','usuario-legado','Loja Legada','legado@example.com','Rua A','81999991001','8h-22h','Goiana','Goiana')")
        database.execSQL("PRAGMA user_version = 2")
        database.close()

        val repo = RepositorioDadosLocais(context)
        val legacy = repo.buscarEmpresa("legada")
        assertEquals("Loja Legada", legacy?.nome)
        assertNull(legacy?.imagemPath)
        val caminho = criarArquivoImagem("empresas/legada.jpg")
        assertTrue(repo.salvarEmpresa(legacy!!.copy(imagemPath = caminho)))
        assertEquals(caminho, repo.buscarEmpresa("legada")?.imagemPath)
        repo.fechar()
    }

    private fun empresaTeste(id: String, usuarioId: String) = Empresa(
        id, usuarioId, "Distribuidora", "loja@example.com", "Rua A", "81999991000", "8h–22h", "Goiana", "Goiana"
    )

    private fun criarArquivoImagem(relativo: String): String {
        val file = File(context.filesDir, "imagens/$relativo")
        file.parentFile?.mkdirs()
        file.writeBytes(byteArrayOf(1, 2, 3, 4))
        return file.canonicalPath
    }
}
