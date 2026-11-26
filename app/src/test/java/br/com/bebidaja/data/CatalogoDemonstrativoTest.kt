package br.com.bebidaja.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogoDemonstrativoTest {
    @Test
    fun demoCatalogCoversAllPlannedCategoriesAndStableIds() {
        assertEquals(
            setOf("Água", "Refrigerante", "Suco", "Energético", "Cerveja", "Destilados", "Outros"),
            demoProducts.mapNotNull { it.categoria }.toSet()
        )
        assertEquals(demoProducts.size, demoProducts.map { it.id }.toSet().size)
        assertTrue(demoProducts.all { it.id.startsWith("demo-${it.empresaId}-") })
    }

    @Test
    fun searchFindsBeverageIgnoringCaseAndAccents() {
        val result = pesquisarCatalogo(
            produtos = demoProducts,
            empresas = demoStores,
            filtro = FiltroPesquisaCatalogo(termo = " AGUA ")
        )

        assertTrue(result.produtos.isNotEmpty())
        assertTrue(result.produtos.all { it.categoria == "Água" })
    }

    @Test
    fun searchByCompanyNameReturnsThatCompanyAndItsProducts() {
        val result = pesquisarCatalogo(
            produtos = demoProducts,
            empresas = demoStores,
            filtro = FiltroPesquisaCatalogo(termo = "ADEGA CENTRAL")
        )

        assertEquals(listOf("adega-central"), result.empresas.map { it.id })
        assertTrue(result.produtos.isNotEmpty())
        assertTrue(result.produtos.all { it.empresaId == "adega-central" })
    }

    @Test
    fun categoryAndCompanyFiltersCombineAndCanBeCleared() {
        val combined = pesquisarCatalogo(
            produtos = demoProducts,
            empresas = demoStores,
            filtro = FiltroPesquisaCatalogo(
                categoria = "Água",
                empresaId = "adega-central"
            )
        )

        assertEquals(listOf("adega-central"), combined.empresas.map { it.id })
        assertEquals(1, combined.produtos.size)
        assertEquals("demo-adega-central-agua", combined.produtos.single().id)

        val cleared = pesquisarCatalogo(demoProducts, demoStores)
        assertEquals(demoStores, cleared.empresas)
        assertEquals(demoProducts, cleared.produtos)
    }

    @Test
    fun selectedCompanyFilterStillAllowsSearchingByProductName() {
        val result = pesquisarCatalogo(
            produtos = demoProducts,
            empresas = demoStores,
            filtro = FiltroPesquisaCatalogo(
                termo = "suco",
                empresaId = "adega-central"
            )
        )

        assertEquals(1, result.produtos.size)
        assertEquals("demo-adega-central-suco", result.produtos.single().id)
    }

    @Test
    fun searchWithoutMatchesReturnsEmptyLists() {
        val result = pesquisarCatalogo(
            produtos = demoProducts,
            empresas = demoStores,
            filtro = FiltroPesquisaCatalogo(termo = "item-que-nao-existe")
        )

        assertTrue(result.empresas.isEmpty())
        assertTrue(result.produtos.isEmpty())
    }

    @Test
    fun unavailableOrIncompleteProductsCannotEnterCart() {
        val unavailable = demoProducts.first { it.categoria == "Energético" }
        val missingPrice = demoProducts.first { it.categoria == "Outros" }
        val available = demoProducts.first { it.categoria == "Água" }

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
        val firstCompanyProduct = demoProducts.first { it.empresaId == "adega-central" }
        val anotherCompanyProduct = demoProducts.first {
            it.empresaId == "mercadinho-do-bairro" && it.disponivel
        }
        val sameCompanyProduct = demoProducts.first {
            it.empresaId == "adega-central" && it.disponivel && it.categoria == "Suco"
        }

        assertTrue(podeAdicionarAoCarrinho(firstCompanyProduct, cart))
        cart.adicionar(mapearProdutoParaCarrinho(firstCompanyProduct)!!)
        assertTrue(podeAdicionarAoCarrinho(sameCompanyProduct, cart))
        assertFalse(podeAdicionarAoCarrinho(anotherCompanyProduct, cart))
    }

    @Test
    fun requiredProductFieldsAndPriceHaveValidationAndFallbacks() {
        val incomplete = ProdutoDemonstrativo(
            id = "incompleto-demo",
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

    @Test
    fun demoRepositoryCreatesUpdatesAvailabilityAndDeletesOnlyWithinCompany() {
        val repository = RepositorioCatalogoDemonstrativo(emptyList())
        val produto = ProdutoDemonstrativo(
            id = "produto-local-demo",
            empresaId = "adega-central",
            nome = "Bebida de teste (demo)",
            volume = "500 ml",
            categoria = "Suco",
            precoCentavos = 525
        )

        assertTrue(repository.salvar(produto))
        assertEquals(produto, repository.buscarPorId(produto.id))
        assertEquals(listOf(produto), repository.listarPorEmpresa("adega-central"))
        assertFalse(
            repository.alterarDisponibilidade(
                produto.id,
                "outra-empresa",
                false
            )
        )
        assertTrue(repository.alterarDisponibilidade(produto.id, produto.empresaId, false))
        assertFalse(repository.buscarPorId(produto.id)?.disponivel ?: true)
        assertFalse(repository.remover(produto.id, "outra-empresa"))
        assertTrue(repository.remover(produto.id, produto.empresaId))
        assertNull(repository.buscarPorId(produto.id))
    }

    @Test
    fun repositoryRejectsInvalidProductsAndCrossCompanyIdReplacement() {
        val repository = RepositorioCatalogoDemonstrativo(
            listOf(
                ProdutoDemonstrativo(
                    id = "produto-estavel",
                    empresaId = "adega-central",
                    nome = "Água (demo)",
                    volume = "500 ml",
                    categoria = "Água",
                    precoCentavos = 300
                )
            )
        )
        val replacement = ProdutoDemonstrativo(
            id = "produto-estavel",
            empresaId = "mercadinho-do-bairro",
            nome = "Outro item (demo)",
            volume = "500 ml",
            categoria = "Água",
            precoCentavos = 300
        )

        assertFalse(repository.salvar(replacement))
        assertFalse(repository.salvar(replacement.copy(id = "novo", nome = " ")))
        assertEquals("adega-central", repository.buscarPorId("produto-estavel")?.empresaId)
    }
}
