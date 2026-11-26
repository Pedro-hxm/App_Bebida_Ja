package br.com.bebidaja.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeEmpresasTest {
    @Test
    fun noSelectedRegionReturnsAllDemonstrativeStores() {
        assertEquals(demoStores, filterDemoStoresByRegion(demoStores, null))
    }

    @Test
    fun selectedRegionReturnsOnlyMatchingStores() {
        val otherRegionStore = demoStores.first().copy(
            id = "estabelecimento-outra-regiao-demo",
            regionId = "outra-regiao-demo"
        )
        val filteredStores = filterDemoStoresByRegion(
            demoStores + otherRegionStore,
            "goiana-demo"
        )

        assertEquals(demoStores, filteredStores)
        assertTrue(filteredStores.all { it.regionId == "goiana-demo" })
    }

    @Test
    fun clearingRegionFilterReturnsAllStoresAgain() {
        val filteredStores = filterDemoStoresByRegion(demoStores, "goiana-demo")

        assertEquals(2, filteredStores.size)
        assertEquals(demoStores, filterDemoStoresByRegion(demoStores, null))
    }

    @Test
    fun unknownRegionReturnsEmptyList() {
        assertTrue(
            filterDemoStoresByRegion(demoStores, "regiao-nao-cadastrada").isEmpty()
        )
    }

    @Test
    fun regionNameLookupReturnsNullForMissingOrUnknownIds() {
        assertEquals("Goiana (demo)", findDemoRegionName("goiana-demo"))
        assertNull(findDemoRegionName(null))
        assertNull(findDemoRegionName("regiao-nao-cadastrada"))
    }

    @Test
    fun missingOrUnknownRegionIsIncludedOnlyWithoutARegionFilter() {
        val storesWithUnassignedRegions = listOf(
            demoStores.first().copy(regionId = null),
            demoStores.last().copy(regionId = "regiao-nao-cadastrada")
        )

        assertEquals(
            storesWithUnassignedRegions,
            filterDemoStoresByRegion(storesWithUnassignedRegions, null)
        )
        assertTrue(
            filterDemoStoresByRegion(storesWithUnassignedRegions, "goiana-demo").isEmpty()
        )
        assertEquals("Região não informada", demoStoreRegionLabel(null))
        assertEquals(
            "Região não informada",
            demoStoreRegionLabel("regiao-nao-cadastrada")
        )
        assertFalse(demoStoreRegionLabel("goiana-demo").contains("não informada"))
    }

    @Test
    fun filteringPreservesStableIdsResolvableByDetailsRoute() {
        val originalIds = setOf("adega-central", "mercadinho-do-bairro")
        val filteredStores = filterDemoStoresByRegion(demoStores, "goiana-demo")

        assertEquals(originalIds, filteredStores.map { it.id }.toSet())
        assertTrue(filteredStores.all { findDemoStoreById(it.id) === it })
    }
}
