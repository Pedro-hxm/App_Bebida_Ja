package br.com.bebidaja.ui.screens

import br.com.bebidaja.data.Empresa
import br.com.bebidaja.data.paraDemoStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TelaDetalhesEmpresaTest {
    @Test
    fun missingOrBlankCompanyFieldsUseFallbackLabel() {
        assertEquals("Não informado", formatarCampoEmpresa(null))
        assertEquals("Não informado", formatarCampoEmpresa(""))
        assertEquals("Não informado", formatarCampoEmpresa("   "))
        assertEquals("Goiana", formatarCampoEmpresa(" Goiana "))
    }

    @Test
    fun persistedCompanyDetailsKeepIdentityAndCommercialFieldsInPresentationAdapter() {
        val empresa = Empresa(
            id = "adega-central",
            usuarioId = "demo-company",
            nome = "Adega Central",
            email = "empresa@demonstracao.com",
            endereco = "Centro, Goiana - PE",
            telefone = "(81) 99999-1001",
            horario = "Todos os dias, 16h às 23h",
            areaEntrega = "Goiana e bairros próximos",
            regiao = "Goiana"
        )

        val detalhes = empresa.paraDemoStore()

        assertEquals(empresa.id, detalhes.id)
        assertEquals(empresa.nome, detalhes.name)
        assertEquals(empresa.endereco, detalhes.address)
        assertEquals(empresa.telefone, detalhes.phone)
        assertEquals(empresa.horario, detalhes.hours)
        assertEquals(empresa.areaEntrega, detalhes.deliveryArea)
        assertEquals("goiana", detalhes.regionId)
        assertNull(detalhes.imageRes)
    }

    @Test
    fun blankPersistedCompanyFieldsAreDisplayedAsNotInformed() {
        assertEquals("Não informado", formatarCampoEmpresa(" "))
    }
}
