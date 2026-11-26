package br.com.bebidaja.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.bebidaja.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var animacaoIniciada by remember { mutableStateOf(false) }
    val opacidade by animateFloatAsState(
        targetValue = if (animacaoIniciada) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "opacidade do mascote"
    )
    val escala by animateFloatAsState(
        targetValue = if (animacaoIniciada) 1f else 0.72f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "escala do mascote"
    )

    LaunchedEffect(Unit) {
        animacaoIniciada = true
        delay(700)
        onFinished()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.mascote_bebida_ja),
            contentDescription = "Mascote do Bebida Já",
            modifier = Modifier
                .size(200.dp)
                .graphicsLayer {
                    scaleX = escala
                    scaleY = escala
                    alpha = opacidade
                },
            contentScale = ContentScale.Fit
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Bebida Já",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text("Bebidas da sua região, em um só lugar.")
    }
}
