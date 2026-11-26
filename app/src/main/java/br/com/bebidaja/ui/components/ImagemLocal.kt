package br.com.bebidaja.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

@Composable
fun SeletorImagemLocal(
    titulo: String,
    subdiretorio: String,
    imagemPath: String?,
    onImagemSelecionada: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var carregando by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            carregando = true
            erro = null
            runCatching { importarImagem(context, uri, subdiretorio) }
                .onSuccess(onImagemSelecionada)
                .onFailure { erro = "Não foi possível salvar essa imagem. Escolha outra." }
            carregando = false
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(titulo, style = MaterialTheme.typography.labelLarge)
        ImagemLocal(imagemPath, contentDescription = titulo, modifier = Modifier.size(112.dp).clip(RoundedCornerShape(12.dp)))
        Button(
            onClick = { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            enabled = !carregando
        ) { Text(if (carregando) "Salvando imagem…" else "Escolher imagem") }
        erro?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

@Composable
fun ImagemLocal(
    imagemPath: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    imagemResFallback: Int? = null
) {
    val imagem = rememberBitmap(imagemPath)
    when {
        imagem != null -> Image(imagem.asImageBitmap(), contentDescription, modifier, contentScale = ContentScale.Crop)
        imagemResFallback != null -> Image(painterResource(imagemResFallback), contentDescription, modifier, contentScale = ContentScale.Crop)
        else -> Surface(modifier, color = MaterialTheme.colorScheme.surfaceVariant) {
            Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("Imagem não informada", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun rememberBitmap(path: String?): Bitmap? {
    var bitmap by remember(path) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(path) {
        bitmap = withContext(Dispatchers.IO) {
            path?.let { runCatching { decodificarImagem(File(it)) }.getOrNull() }
        }
    }
    return bitmap
}

private suspend fun importarImagem(context: Context, origem: Uri, subdiretorio: String): String =
    withContext(Dispatchers.IO) {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(origem)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: error("Arquivo de imagem indisponível")
        options.inSampleSize = amostra(options.outWidth, options.outHeight, 1600)
        options.inJustDecodeBounds = false
        val bitmap = context.contentResolver.openInputStream(origem)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: error("Não foi possível decodificar a imagem")
        val pasta = File(context.filesDir, "imagens/$subdiretorio").apply { mkdirs() }
        val arquivo = File(pasta, "${UUID.randomUUID()}.jpg")
        arquivo.outputStream().use { output ->
            check(bitmap.compress(Bitmap.CompressFormat.JPEG, 88, output)) { "Falha ao gravar a imagem" }
        }
        bitmap.recycle()
        arquivo.canonicalPath
    }

private fun decodificarImagem(arquivo: File): Bitmap? {
    if (!arquivo.isFile) return null
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(arquivo.absolutePath, bounds)
    val options = BitmapFactory.Options().apply { inSampleSize = amostra(bounds.outWidth, bounds.outHeight, 1600) }
    return BitmapFactory.decodeFile(arquivo.absolutePath, options)
}

private fun amostra(largura: Int, altura: Int, maximo: Int): Int {
    var sample = 1
    while (largura / sample > maximo || altura / sample > maximo) sample *= 2
    return sample
}

fun caminhoImagemDoApp(context: Context, caminho: String?): Boolean {
    if (caminho.isNullOrBlank()) return true
    val raiz = File(context.filesDir, "imagens").canonicalFile
    val arquivo = File(caminho).canonicalFile
    return arquivo.path.startsWith(raiz.path + File.separator) && arquivo.isFile
}
