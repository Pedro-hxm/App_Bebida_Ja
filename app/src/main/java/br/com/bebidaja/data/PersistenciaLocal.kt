package br.com.bebidaja.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.UUID

data class Empresa(
    val id: String,
    val usuarioId: String,
    val nome: String,
    val email: String,
    val endereco: String,
    val telefone: String,
    val horario: String,
    val areaEntrega: String,
    val regiao: String = "Goiana"
)

data class ProdutoCatalogo(
    val id: String,
    val empresaId: String,
    val nome: String,
    val descricao: String,
    val categoria: String,
    val precoCentavos: Long,
    val disponivel: Boolean = true,
    val volume: String = descricao,
    val imagemRes: Int? = null
)

data class DadosComerciaisEmpresa(
    val nome: String,
    val endereco: String,
    val telefone: String,
    val horario: String,
    val areaEntrega: String
)

fun Empresa.paraDemoStore() = DemoStore(
    id = id,
    name = nome,
    description = "Entrega em Goiana",
    address = endereco,
    phone = telefone,
    hours = horario,
    deliveryArea = areaEntrega,
    regionId = regiao
)

fun ProdutoCatalogo.paraProdutoDemonstrativo() = ProdutoDemonstrativo(
    id = id,
    empresaId = empresaId,
    nome = nome,
    descricao = descricao,
    volume = volume,
    categoria = categoria,
    precoCentavos = precoCentavos,
    disponivel = disponivel,
    imagemRes = imagemRes
)

fun ProdutoDemonstrativo.paraProdutoLocal(): ProdutoCatalogo? {
    if (validarCamposProduto(this).isNotEmpty()) return null
    return ProdutoCatalogo(
        id = id,
        empresaId = empresaId,
        nome = nome!!.trim(),
        descricao = descricao.orEmpty().trim(),
        categoria = categoria!!,
        precoCentavos = precoCentavos!!,
        disponivel = disponivel,
        volume = volume!!.trim(),
        imagemRes = imagemRes
    )
}

/** Banco local durável do protótipo. Não sincroniza entre dispositivos. */
class RepositorioDadosLocais(context: Context) : RepositorioPedidos {
    private val helper = BancoLocal(context.applicationContext)

    fun salvarEmpresa(empresa: Empresa) {
        helper.writableDatabase.insertWithOnConflict(
            "empresas", null, ContentValues().apply {
                put("id", empresa.id)
                put("usuario_id", empresa.usuarioId)
                put("nome", empresa.nome)
                put("email", empresa.email)
                put("endereco", empresa.endereco)
                put("telefone", empresa.telefone)
                put("horario", empresa.horario)
                put("area_entrega", empresa.areaEntrega)
                put("regiao", empresa.regiao)
                put("ativa", 1)
            }, SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun buscarEmpresa(id: String): Empresa? = helper.readableDatabase.rawQuery(
        "SELECT * FROM empresas WHERE id = ? AND ativa = 1", arrayOf(id)
    ).use { cursor -> if (cursor.moveToFirst()) cursor.empresa() else null }

    fun empresaDoUsuario(usuarioId: String): Empresa? = helper.readableDatabase.rawQuery(
        "SELECT * FROM empresas WHERE usuario_id = ? AND ativa = 1 LIMIT 1", arrayOf(usuarioId)
    ).use { cursor -> if (cursor.moveToFirst()) cursor.empresa() else null }

    fun listarEmpresas(): List<Empresa> = helper.readableDatabase.rawQuery(
        "SELECT * FROM empresas WHERE ativa = 1 ORDER BY nome COLLATE NOCASE", null
    ).use { cursor -> buildList { while (cursor.moveToNext()) add(cursor.empresa()) } }

    fun salvarProduto(produto: ProdutoCatalogo): Boolean {
        if (produto.id.isBlank() || produto.empresaId.isBlank() || produto.nome.isBlank() || produto.volume.isBlank() || produto.categoria !in demoCategories || produto.precoCentavos <= 0L) return false
        val existente = helper.readableDatabase.rawQuery("SELECT empresa_id FROM produtos WHERE id = ?", arrayOf(produto.id)).use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
        if (existente != null && existente != produto.empresaId) return false
        val result = helper.writableDatabase.insertWithOnConflict(
            "produtos", null, ContentValues().apply {
                put("id", produto.id)
                put("empresa_id", produto.empresaId)
                put("nome", produto.nome)
                put("descricao", produto.descricao)
                put("categoria", produto.categoria)
                put("preco_centavos", produto.precoCentavos)
                put("disponivel", if (produto.disponivel) 1 else 0)
                put("volume", produto.volume)
                if (produto.imagemRes == null) putNull("imagem_res") else put("imagem_res", produto.imagemRes)
            }, SQLiteDatabase.CONFLICT_REPLACE
        )
        return result != -1L
    }

    fun listarProdutos(empresaId: String? = null, somenteDisponiveis: Boolean = false): List<ProdutoCatalogo> {
        val where = buildList {
            if (empresaId != null) add("empresa_id = ?")
            if (somenteDisponiveis) add("disponivel = 1")
        }.joinToString(" AND ").ifBlank { "1 = 1" }
        val args = if (empresaId == null) emptyArray() else arrayOf(empresaId)
        return helper.readableDatabase.rawQuery(
            "SELECT * FROM produtos WHERE $where ORDER BY nome COLLATE NOCASE", args
        ).use { cursor -> buildList { while (cursor.moveToNext()) add(cursor.produto()) } }
    }

    fun removerProduto(produtoId: String, empresaId: String): Boolean =
        helper.writableDatabase.delete("produtos", "id = ? AND empresa_id = ?", arrayOf(produtoId, empresaId)) == 1

    fun definirDisponibilidade(produtoId: String, empresaId: String, disponivel: Boolean): Boolean =
        helper.writableDatabase.update(
            "produtos", ContentValues().apply { put("disponivel", if (disponivel) 1 else 0) },
            "id = ? AND empresa_id = ?", arrayOf(produtoId, empresaId)
        ) == 1

    override fun buscarPorChaveConfirmacao(chave: String): Pedido? =
        helper.readableDatabase.rawQuery("SELECT * FROM pedidos WHERE chave = ?", arrayOf(chave))
            .use { cursor -> if (cursor.moveToFirst()) cursor.pedido() else null }

    override fun salvar(pedido: Pedido): Pedido {
        buscarPorChaveConfirmacao(pedido.chaveConfirmacao)?.let { return it }
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            db.insertOrThrow("pedidos", null, ContentValues().apply {
                put("id", pedido.id); put("chave", pedido.chaveConfirmacao)
                put("cliente_id", pedido.clienteId); put("empresa_id", pedido.empresaId)
                put("rua", pedido.endereco.rua); put("numero", pedido.endereco.numero)
                put("bairro", pedido.endereco.bairro); put("cidade", pedido.endereco.cidade)
                put("cep", pedido.endereco.cep); put("complemento", pedido.endereco.complemento)
                put("subtotal", pedido.subtotalCentavos); put("total", pedido.totalCentavos)
                put("pagamento", pedido.formaPagamento.name); put("comprovante", pedido.referenciaComprovante)
                put("status", pedido.status.name); put("criado_em", pedido.criadoEmMillis)
            })
            pedido.itens.forEach { item -> db.insertOrThrow("itens_pedido", null, ContentValues().apply {
                put("pedido_id", pedido.id); put("produto_id", item.produtoId)
                put("nome", item.nomeProduto); put("preco", item.precoUnitarioCentavos)
                put("quantidade", item.quantidade)
            }) }
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
        return pedido
    }

    fun confirmarPedido(
        clienteId: String,
        carrinho: Carrinho,
        endereco: EnderecoEntrega?,
        formaPagamento: FormaPagamento?,
        referenciaComprovante: String,
        chaveConfirmacao: String,
        criadoEmMillis: Long = System.currentTimeMillis()
    ): Result<Pedido> {
        if (chaveConfirmacao.isBlank()) return Result.failure(IllegalArgumentException("Confirmação inválida."))
        buscarPorChaveConfirmacao(chaveConfirmacao)?.let { return Result.success(it) }
        if (clienteId.isBlank() || carrinho.estaVazio || endereco?.estaValido != true || formaPagamento == null) {
            return Result.failure(IllegalArgumentException("Dados do pedido inválidos."))
        }
        if (!referenciaComprovante.startsWith("DEMO-")) {
            return Result.failure(IllegalArgumentException("O comprovante deve ser demonstrativo."))
        }
        val empresas = carrinho.itens.map { it.produto.empresaId }.distinct()
        if (empresas.size != 1) return Result.failure(IllegalArgumentException("Use produtos de uma única empresa."))
        val itens = carrinho.itens.map {
            ItemPedido(it.produto.produtoId, it.produto.nome, it.produto.precoUnitarioCentavos, it.quantidade)
        }
        val subtotal = itens.sumOf { it.subtotalCentavos }
        return runCatching { salvar(Pedido(
            id = UUID.randomUUID().toString(), chaveConfirmacao = chaveConfirmacao,
            clienteId = clienteId, empresaId = empresas.single(), itens = itens,
            endereco = endereco, subtotalCentavos = subtotal, totalCentavos = subtotal,
            formaPagamento = formaPagamento, referenciaComprovante = referenciaComprovante,
            status = StatusPedido.RECEBIDO, criadoEmMillis = criadoEmMillis
        )) }
    }

    override fun listarPorCliente(clienteId: String): List<Pedido> = listarPedidos("cliente_id", clienteId)

    fun listarPorEmpresa(empresaId: String): List<Pedido> = listarPedidos("empresa_id", empresaId)

    fun atualizarStatus(pedidoId: String, empresaId: String, novoStatus: StatusPedido): Boolean {
        val atual = listarPorEmpresa(empresaId).firstOrNull { it.id == pedidoId } ?: return false
        val permitido = when (atual.status) {
            StatusPedido.RECEBIDO -> novoStatus == StatusPedido.EM_PREPARACAO
            StatusPedido.EM_PREPARACAO -> novoStatus == StatusPedido.SAIU_PARA_ENTREGA
            StatusPedido.SAIU_PARA_ENTREGA -> novoStatus == StatusPedido.ENTREGUE
            StatusPedido.ENTREGUE -> false
        }
        if (!permitido) return false
        return helper.writableDatabase.update(
            "pedidos", ContentValues().apply { put("status", novoStatus.name) },
            "id = ? AND empresa_id = ? AND status = ?",
            arrayOf(pedidoId, empresaId, atual.status.name)
        ) == 1
    }

    fun fechar() = helper.close()

    private fun listarPedidos(coluna: String, valor: String): List<Pedido> = helper.readableDatabase.rawQuery(
        "SELECT * FROM pedidos WHERE $coluna = ? ORDER BY criado_em DESC", arrayOf(valor)
    ).use { cursor -> buildList { while (cursor.moveToNext()) add(cursor.pedido()) } }

    private fun android.database.Cursor.empresa() = Empresa(
        getString(getColumnIndexOrThrow("id")), getString(getColumnIndexOrThrow("usuario_id")),
        getString(getColumnIndexOrThrow("nome")), getString(getColumnIndexOrThrow("email")),
        getString(getColumnIndexOrThrow("endereco")), getString(getColumnIndexOrThrow("telefone")),
        getString(getColumnIndexOrThrow("horario")), getString(getColumnIndexOrThrow("area_entrega")),
        getString(getColumnIndexOrThrow("regiao"))
    )

    private fun android.database.Cursor.produto() = ProdutoCatalogo(
        getString(getColumnIndexOrThrow("id")), getString(getColumnIndexOrThrow("empresa_id")),
        getString(getColumnIndexOrThrow("nome")), getString(getColumnIndexOrThrow("descricao")),
        getString(getColumnIndexOrThrow("categoria")), getLong(getColumnIndexOrThrow("preco_centavos")),
        getInt(getColumnIndexOrThrow("disponivel")) == 1,
        getString(getColumnIndexOrThrow("volume")),
        if (isNull(getColumnIndexOrThrow("imagem_res"))) null else getInt(getColumnIndexOrThrow("imagem_res"))
    )

    private fun android.database.Cursor.pedido(): Pedido {
        val id = getString(getColumnIndexOrThrow("id"))
        val itens = helper.readableDatabase.rawQuery(
            "SELECT * FROM itens_pedido WHERE pedido_id = ?", arrayOf(id)
        ).use { cursor -> buildList { while (cursor.moveToNext()) add(ItemPedido(
            cursor.getString(cursor.getColumnIndexOrThrow("produto_id")),
            cursor.getString(cursor.getColumnIndexOrThrow("nome")),
            cursor.getLong(cursor.getColumnIndexOrThrow("preco")),
            cursor.getInt(cursor.getColumnIndexOrThrow("quantidade"))
        )) } }
        return Pedido(
            id = id, chaveConfirmacao = getString(getColumnIndexOrThrow("chave")),
            clienteId = getString(getColumnIndexOrThrow("cliente_id")),
            empresaId = getString(getColumnIndexOrThrow("empresa_id")), itens = itens,
            endereco = EnderecoEntrega(
                getString(getColumnIndexOrThrow("rua")), getString(getColumnIndexOrThrow("numero")),
                getString(getColumnIndexOrThrow("bairro")), getString(getColumnIndexOrThrow("cidade")),
                getString(getColumnIndexOrThrow("cep")), getString(getColumnIndexOrThrow("complemento"))
            ), subtotalCentavos = getLong(getColumnIndexOrThrow("subtotal")),
            totalCentavos = getLong(getColumnIndexOrThrow("total")),
            formaPagamento = FormaPagamento.valueOf(getString(getColumnIndexOrThrow("pagamento"))),
            referenciaComprovante = getString(getColumnIndexOrThrow("comprovante")),
            status = StatusPedido.valueOf(getString(getColumnIndexOrThrow("status"))),
            criadoEmMillis = getLong(getColumnIndexOrThrow("criado_em"))
        )
    }
}

private class BancoLocal(context: Context) : SQLiteOpenHelper(context, "bebidaja.db", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE empresas (id TEXT PRIMARY KEY, usuario_id TEXT NOT NULL UNIQUE, nome TEXT NOT NULL, email TEXT NOT NULL, endereco TEXT NOT NULL, telefone TEXT NOT NULL, horario TEXT NOT NULL, area_entrega TEXT NOT NULL, regiao TEXT NOT NULL, ativa INTEGER NOT NULL DEFAULT 1)")
        db.execSQL("CREATE TABLE produtos (id TEXT PRIMARY KEY, empresa_id TEXT NOT NULL, nome TEXT NOT NULL, descricao TEXT NOT NULL, volume TEXT NOT NULL DEFAULT '', categoria TEXT NOT NULL, preco_centavos INTEGER NOT NULL CHECK(preco_centavos >= 0), disponivel INTEGER NOT NULL DEFAULT 1, imagem_res INTEGER, FOREIGN KEY(empresa_id) REFERENCES empresas(id))")
        db.execSQL("CREATE INDEX produtos_empresa_idx ON produtos(empresa_id)")
        db.execSQL("CREATE TABLE pedidos (id TEXT PRIMARY KEY, chave TEXT NOT NULL UNIQUE, cliente_id TEXT NOT NULL, empresa_id TEXT NOT NULL, rua TEXT NOT NULL, numero TEXT NOT NULL, bairro TEXT NOT NULL, cidade TEXT NOT NULL, cep TEXT NOT NULL, complemento TEXT NOT NULL, subtotal INTEGER NOT NULL, total INTEGER NOT NULL, pagamento TEXT NOT NULL, comprovante TEXT NOT NULL, status TEXT NOT NULL, criado_em INTEGER NOT NULL)")
        db.execSQL("CREATE INDEX pedidos_cliente_idx ON pedidos(cliente_id, criado_em DESC)")
        db.execSQL("CREATE INDEX pedidos_empresa_idx ON pedidos(empresa_id, criado_em DESC)")
        db.execSQL("CREATE TABLE itens_pedido (pedido_id TEXT NOT NULL, produto_id TEXT NOT NULL, nome TEXT NOT NULL, preco INTEGER NOT NULL, quantidade INTEGER NOT NULL CHECK(quantidade > 0), PRIMARY KEY(pedido_id, produto_id), FOREIGN KEY(pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE)")
        inserirDadosDemonstrativos(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE produtos ADD COLUMN volume TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE produtos ADD COLUMN imagem_res INTEGER")
        }
    }

    private fun inserirDadosDemonstrativos(db: SQLiteDatabase) {
        val empresas: List<Array<Any>> = listOf(
            arrayOf<Any>("adega-central", "demo-company", "Adega Central", "empresa@demonstracao.com", "Centro, Goiana - PE", "(81) 99999-1001", "Todos os dias, 16h às 23h", "Goiana e bairros próximos", "Goiana"),
            arrayOf<Any>("mercadinho-do-bairro", "demo-market", "Mercadinho do Bairro", "mercado@demonstracao.com", "Rua do Comércio, Goiana - PE", "(81) 99999-1002", "Segunda a sábado, 8h às 20h", "Goiana", "Goiana")
        )
        empresas.forEach { e -> db.execSQL("INSERT INTO empresas(id,usuario_id,nome,email,endereco,telefone,horario,area_entrega,regiao) VALUES(?,?,?,?,?,?,?,?,?)", e) }
        val produtos: List<Array<Any>> = listOf(
            arrayOf<Any>("agua-adega", "adega-central", "Água mineral", "Água mineral", "1,5 L", "Água", 450L, 1),
            arrayOf<Any>("refri-adega", "adega-central", "Refrigerante", "Refrigerante", "2 L", "Refrigerante", 990L, 1),
            arrayOf<Any>("suco-adega", "adega-central", "Suco de uva", "Suco de uva", "1 L", "Suco", 750L, 1),
            arrayOf<Any>("agua-mercado", "mercadinho-do-bairro", "Água mineral", "Água mineral", "1,5 L", "Água", 400L, 1)
        )
        produtos.forEach { p -> db.execSQL("INSERT INTO produtos(id,empresa_id,nome,descricao,volume,categoria,preco_centavos,disponivel) VALUES(?,?,?,?,?,?,?,?)", p) }
    }
}
