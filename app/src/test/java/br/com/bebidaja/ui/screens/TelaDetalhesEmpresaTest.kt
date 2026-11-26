package br.com.bebidaja.ui.screens

import br.com.bebidaja.data.demoStores
import br.com.bebidaja.data.findDemoStoreById
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class TelaDetalhesEmpresaTest {
    @Test
    fun demoStoresHaveStableUniqueIds() {
        val ids = demoStores.map { it.id }

        assertTrue(ids.all(String::isNotBlank))
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun findDemoStoreByIdReturnsMatchingStore() {
        demoStores.forEach { store ->
            assertSame(store, findDemoStoreById(store.id))
        }
    }

    @Test
    fun findDemoStoreByIdReturnsNullForUnknownId() {
        assertNull(findDemoStoreById("empresa-inexistente"))
    }

    @Test
    fun missingOrBlankCompanyFieldsUseFallbackLabel() {
        assertEquals("Não informado", formatarCampoEmpresa(null))
        assertEquals("Não informado", formatarCampoEmpresa("   "))
        assertFalse(formatarCampoEmpresa("Goiana").isBlank())
        assertEquals("Goiana", formatarCampoEmpresa("Goiana"))
    }
}
