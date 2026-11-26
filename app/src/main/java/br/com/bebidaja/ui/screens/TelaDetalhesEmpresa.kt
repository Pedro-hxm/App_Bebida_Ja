package br.com.bebidaja.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import br.com.bebidaja.data.DemoStore

@Composable
fun TelaDetalhesEmpresa(
    empresa: DemoStore?,
    onVoltar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onVoltar) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
            }
            Text(
                text = empresa?.name ?: "Estabelecimento não encontrado",
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (empresa == null) {
            Text(
                text = "Não encontramos esse estabelecimento.",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            ImagemEmpresa(empresa)
            Text(
                text = empresa.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text("Informações comerciais", style = MaterialTheme.typography.titleMedium)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CampoInformacaoEmpresa("Endereço", formatarCampoEmpresa(empresa.address))
                    CampoInformacaoEmpresa("Telefone", formatarCampoEmpresa(empresa.phone))
                    CampoInformacaoEmpresa("Horário", formatarCampoEmpresa(empresa.hours))
                    CampoInformacaoEmpresa(
                        "Área de entrega",
                        formatarCampoEmpresa(empresa.deliveryArea)
                    )
                }
            }
        }
    }
}

@Composable
private fun ImagemEmpresa(empresa: DemoStore) {
    val imageRes = empresa.imageRes
    if (imageRes != null) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = "Imagem de ${empresa.name}",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "Imagem não informada",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CampoInformacaoEmpresa(rotulo: String, valor: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = rotulo,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = valor, style = MaterialTheme.typography.bodyLarge)
    }
}

internal fun formatarCampoEmpresa(valor: String?): String =
    valor?.takeIf { it.isNotBlank() } ?: "Não informado"
