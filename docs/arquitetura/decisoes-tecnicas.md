# Decisões técnicas da branch

## Interface

O aplicativo usa Jetpack Compose com Material 3 e Navigation Compose.

## Ambiente

A configuração usa Android Gradle Plugin 9.2.0, Kotlin com compilador Compose, Gradle 9.4.1, SDK 37 e Java 17. O SDK mínimo é 24.

## Persistência e autenticação

Empresas, catálogo, pedidos e itens de pedido usam SQLite por meio de `SQLiteOpenHelper`. A escolha dá persistência durável no aparelho sem adicionar dependências. Os itens do pedido são snapshots para preservar nome e preço no histórico. A transição de status é sequencial e vinculada ao identificador da empresa.

Firebase Authentication continua responsável pelas contas quando `google-services.json` está configurado. O documento Firestore guarda nome, e-mail e tipo de perfil. Dados comerciais, produtos e pedidos permanecem no SQLite local; esta implementação não sincroniza aparelhos.

Pagamento e comprovante seguem demonstrativos. Não há cobrança real, upload de arquivo nem servidor de catálogo/pedidos nesta branch.
