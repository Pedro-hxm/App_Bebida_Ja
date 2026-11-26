package br.com.bebidaja.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeEmpresasTest {
    private val empresasPersistidas = listOf(
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

    @Test
    fun regionOptionsAndCardsUseTheSamePersistedRegionIdentity() {
        val regioes = listarRegioesPersistidas(empresasPersistidas)
        val regionIdFromCard = empresasPersistidas.first().paraDemoStore().regionId

        assertEquals(listOf(RegiaoPersistida(id = "goiana", nome = "Goiana")), regioes)
        assertEquals("goiana", regionIdFromCard)
        assertEquals(empresasPersistidas, filtrarEmpresasPorRegiao(empresasPersistidas, regionIdFromCard))
    }

    @Test
    fun persistedAndLegacyRegionIdsResolveToTheSameCompanies() {
        assertEquals(
            empresasPersistidas,
            filtrarEmpresasPorRegiao(empresasPersistidas, "goiana")
        )
        assertEquals(
            empresasPersistidas,
            filtrarEmpresasPorRegiao(empresasPersistidas, "goiana-demo")
        )
    }

    @Test
    fun noRegionFilterAndClearingReturnAllPersistedCompanies() {
        val filtered = filtrarEmpresasPorRegiao(empresasPersistidas, "goiana")

        assertEquals(empresasPersistidas, filtrarEmpresasPorRegiao(empresasPersistidas, null))
        assertEquals(empresasPersistidas, filtrarEmpresasPorRegiao(empresasPersistidas, ""))
        assertEquals(empresasPersistidas, filtrarEmpresasPorRegiao(empresasPersistidas, null))
        assertTrue(filtered.isNotEmpty())
    }

    @Test
    fun unknownRegionReturnsNoCompanies() {
        assertTrue(
            filtrarEmpresasPorRegiao(empresasPersistidas, "regiao-inexistente").isEmpty()
        )
    }

    @Test
    fun missingRegionRemainsUnassignedAndDoesNotCreateAnOption() {
        val semRegiao = empresasPersistidas.first().copy(regiao = "")
        val semRegiaoStore = semRegiao.paraDemoStore()

        assertTrue(listarRegioesPersistidas(listOf(semRegiao)).isEmpty())
        assertNull(semRegiaoStore.regionId)
        assertEquals("Região não informada", demoStoreRegionLabel(semRegiaoStore.regionId))
        assertEquals(listOf(semRegiao), filtrarEmpresasPorRegiao(listOf(semRegiao), null))
        assertTrue(filtrarEmpresasPorRegiao(listOf(semRegiao), "goiana").isEmpty())
    }

    @Test
    fun regionFilteringPreservesPersistedCompanyIds() {
        val filtered = filtrarEmpresasPorRegiao(empresasPersistidas, "goiana")

        assertEquals(
            setOf("adega-central", "mercadinho-do-bairro"),
            filtered.map { it.id }.toSet()
        )
    }
}
