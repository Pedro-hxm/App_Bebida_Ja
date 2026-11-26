package br.com.bebidaja.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogoDemonstrativoTest {
    private val empresas = listOf(
        Empresa(
            id = "adega-central",
            usuarioId = "demo-company",
            nome = "Adega Central",
            email = "empresa@demonstracao.com",
            endereco = "Centro, Goiana - PE",
            telefone = "(81) 99999-1001",
            horario = "Todos os dias, 16h às 23h",
            areaEntrega = "Goiana e bairros próximos",
            regiao = "Goiana"
        ),
        Empresa(
            id = "mercadinho-do-bairro",
            usuarioId = "demo-market",
            nome = "Mercadinho do Bairro",
            email = "mercado@demonstracao.com",
            endereco = "Rua do Comércio, Goiana - PE",
            telefone = "(81) 99999-1002",
            horario = "Segunda a sábado, 8h às 20h",
            areaEntrega = "Goiana",
            regiao = "Goiana"
        )
    )
    private val lojas = empresas.map(Empresa::paraDemoStore)
    private val produtosPersistidos = listOf(
        ProdutoCatalogo(
            id = "agua-adega",
            empresaId = "adega-central",
            nome = "Água mineral",
            descricao = "Água mineral",
            categoria = "Água",
            precoCentavos = 450,
            volume = "1,5 L",
            imagemRes = 101
        ),
        ProdutoCatalogo(
            id = "refri-adega",
            empresaId = "adega-central",
            nome = "Refrigerante",
            descricao = "Refrigerante",
            categoria = "Refrigerante",
            precoCentavos = 990,
            volume = "2 L"
        ),
        ProdutoCatalogo(
            id = "suco-adega",
            empresaId = "adega-central",
            nome = "Suco de uva",
            descricao = "Suco de uva",
            categoria = "Suco",
            precoCentavos = 750,
            volume = "1 L"
        ),
        ProdutoCatalogo(
            id = "agua-mercado",
            empresaId = "mercadinho-do-bairro",
            nome = "Água mineral",
            descricao = "Água mineral",
            categoria = "Água",
            precoCentavos = 400,
            volume = "1,5 L"
        )
    )
    private val produtos = produtosPersistidos.map(ProdutoCatalogo::paraProdutoDemonstrativo)

    @Test
    fun categoriesMatchThePersistedCatalogContractAndIdsRemainStable() {
        assertEquals(
            setOf("Água", "Refrigerante", "Suco", "Energético", "Cerveja", "Destilados", "Outros"),
            demoCategories.toSet()
        )
        assertEquals(
            listOf("agua-adega", "refri-adega", "suco-adega", "agua-mercado"),
            produtos.map { it.id }
        )
        assertEquals("adega-central", produtos.first().empresaId)
        assertEquals("mercadinho-do-bairro", produtos.last().empresaId)
    }

    @Test
    fun productAdapterRoundTripPreservesPersistedFieldsAndImageResource() {
        val persisted = produtosPersistidos.first()
        val roundTrip = persisted
            .paraProdutoDemonstrativo()
            .paraProdutoLocal()

        assertEquals(persisted, roundTrip)
        assertEquals(101, roundTrip?.imagemRes)
    }

    @Test
    fun searchFindsPersistedBeverageIgnoringCaseAndAccents() {
        val result = pesquisarCatalogo(
            produtos = produtos,
            empresas = lojas,
            filtro = FiltroPesquisaCatalogo(termo = " AGUA ")
        )

        assertEquals(setOf("agua-adega", "agua-mercado"), result.produtos.map { it.id }.toSet())
        assertTrue(result.produtos.all { it.categoria == "Água" })
    }

    @Test
    fun searchByPersistedCompanyNameReturnsItsCompanyAndProducts() {
        val result = pesquisarCatalogo(
            produtos = produtos,
            empresas = lojas,
            filtro = FiltroPesquisaCatalogo(termo = "ADEGA CENTRAL")
        )

        assertEquals(listOf("adega-central"), result.empresas.map { it.id })
        assertEquals(setOf("agua-adega", "refri-adega", "suco-adega"), result.produtos.map { it.id }.toSet())
        assertTrue(result.produtos.all { it.empresaId == "adega-central" })
    }

    @Test
    fun categoryAndCompanyFiltersCombineAndClearingRestoresAllPersistedRows() {
        val combined = pesquisarCatalogo(
            produtos = produtos,
            empresas = lojas,
            filtro = FiltroPesquisaCatalogo(categoria = "Água", empresaId = "adega-central")
        )

        assertEquals(listOf("adega-central"), combined.empresas.map { it.id })
        assertEquals(listOf("agua-adega"), combined.produtos.map { it.id })

        val cleared = pesquisarCatalogo(produtos, lojas)
        assertEquals(lojas, cleared.empresas)
        assertEquals(produtos, cleared.produtos)
    }

    @Test
    fun selectedCompanyFilterStillAllowsSearchingByProductName() {
        val result = pesquisarCatalogo(
            produtos = produtos,
            empresas = lojas,
            filtro = FiltroPesquisaCatalogo(
                termo = "suco",
                empresaId = "adega-central"
            )
        )

        assertEquals(listOf("suco-adega"), result.produtos.map { it.id })
    }

    @Test
    fun searchWithoutMatchesReturnsEmptyLists() {
        val result = pesquisarCatalogo(
            produtos = produtos,
            empresas = lojas,
            filtro = FiltroPesquisaCatalogo(termo = "item-que-nao-existe")
        )

        assertTrue(result.empresas.isEmpty())
        assertTrue(result.produtos.isEmpty())
    }

    @Test
    fun searchDoesNotExposeProductsWithoutTheirPersistedCompany() {
        val orphanedProduct = produtos.first().copy(
            id = "produto-sem-empresa",
            empresaId = "empresa-inexistente"
        )

        val result = pesquisarCatalogo(
            produtos = produtos + orphanedProduct,
            empresas = lojas
        )

        assertFalse(result.produtos.any { it.id == orphanedProduct.id })
    }

    @Test
    fun unavailableOrIncompleteProductsCannotEnterCart() {
        val unavailable = produtos.first().copy(disponivel = false)
        val missingPrice = ProdutoDemonstrativo(
            id = "produto-sem-preco",
            empresaId = "adega-central",
            nome = "Bebida",
            volume = "500 ml",
            categoria = "Água",
            precoCentavos = null
        )
        val available = produtos.first()

        assertNull(mapearProdutoParaCarrinho(unavailable))
        assertNull(mapearProdutoParaCarrinho(missingPrice))

        val cartProduct = mapearProdutoParaCarrinho(available)
        assertNotNull(cartProduct)
        assertEquals(available.id, cartProduct?.produtoId)
        assertEquals(available.empresaId, cartProduct?.empresaId)
        assertEquals(available.precoCentavos, cartProduct?.precoUnitarioCentavos)
    }

    @Test
    fun cartKeepsTheExistingSingleCompanyPerOrderRule() {
        val cart = Carrinho()
        val firstCompanyProduct = produtos.first { it.empresaId == "adega-central" }
        val anotherCompanyProduct = produtos.first { it.empresaId == "mercadinho-do-bairro" }
        val sameCompanyProduct = produtos.first {
            it.empresaId == "adega-central" && it.categoria == "Suco"
        }

        assertTrue(podeAdicionarAoCarrinho(firstCompanyProduct, cart))
        cart.adicionar(mapearProdutoParaCarrinho(firstCompanyProduct)!!)
        assertTrue(podeAdicionarAoCarrinho(sameCompanyProduct, cart))
        assertFalse(podeAdicionarAoCarrinho(anotherCompanyProduct, cart))
    }

    @Test
    fun requiredFieldsAndPriceKeepValidationAndFallbacks() {
        val incomplete = ProdutoDemonstrativo(
            id = "produto-incompleto",
            empresaId = "adega-central",
            nome = null,
            volume = null,
            categoria = null,
            precoCentavos = null
        )

        assertEquals(setOf("Nome", "Volume", "Categoria", "Preço"), validarCamposProduto(incomplete).toSet())
        assertEquals("Não informado", incomplete.name)
        assertEquals("Não informado", formatarPreco(null))
        assertEquals(990L, converterPrecoParaCentavos("9,90"))
        assertEquals(123456L, converterPrecoParaCentavos("1.234,56"))
        assertEquals(990L, converterPrecoParaCentavos("9.90"))
        assertNull(converterPrecoParaCentavos("texto"))
        assertNull(converterPrecoParaCentavos("0,00"))
        assertNull(converterPrecoParaCentavos("9,999"))
    }
}
