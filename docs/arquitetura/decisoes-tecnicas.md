# Decisões técnicas da base

## Interface

Foi escolhido Jetpack Compose com Material 3 para construir a interface em Kotlin e permitir que o grupo evolua as telas no Android Studio. A navegação entre telas usa Navigation Compose.

## Ambiente

A configuração inicial usa Android Gradle Plugin 9.2.0, Kotlin integrado ao plugin Android com o compilador Compose 2.2.10, Gradle 9.4.1, SDK 37 e Java 17. O SDK mínimo definido é 24.

Na primeira sincronização, o Gradle pode precisar baixar a distribuição e dependências. O projeto não inclui credenciais nem configuração local do Android SDK.

## Limites

Os dados de exemplo ficam em memória no código. Esta escolha mantém a base pequena e não define a persistência do produto final. Room, API, Firebase, autenticação real e pagamentos não foram configurados.