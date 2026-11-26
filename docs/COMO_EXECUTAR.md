# Abrir e executar o BebidaJá no Android Studio

Este passo a passo cobre a versão integrada local. O acesso demonstrativo não depende de conta; o acesso real depende da configuração Firebase. Empresas, catálogo e pedidos ficam no SQLite do aparelho e não sincronizam entre dispositivos.

## 1. Abra a pasta correta

No Android Studio, escolha File > Open e selecione a pasta raiz App_Bebida_Ja. É a pasta que contém settings.gradle.kts, build.gradle.kts e gradlew.bat. Não abra somente a pasta app.

## 2. Aguarde a sincronização do Gradle

Quando o Android Studio perguntar, escolha Trust Project e aguarde a sincronização terminar. Na primeira abertura, o Gradle baixa a distribuição e as dependências; mantenha a internet conectada.

Se o Gradle informar erro de Java, abra File > Settings > Build, Execution, Deployment > Build Tools > Gradle e selecione o Embedded JDK do Android Studio em Gradle JDK. O Android Studio recomenda o JBR integrado; este projeto configura compatibilidade de código Java 17.

## 3. Instale o SDK e o emulador

1. Abra Tools > SDK Manager.
2. Em SDK Platforms, marque Android API 37.
3. Em SDK Tools, confirme Android SDK Platform-Tools, Android SDK Build-Tools e Android Emulator.
4. Clique Apply e aceite as licenças solicitadas.

O aplicativo compila com API 37 e pode ser executado em emuladores Android API 24 ou superior. Se uma imagem de sistema API 37 não estiver disponível, escolha a versão mais recente oferecida pelo Android Studio.

## 4. Crie e inicie um dispositivo virtual

1. Abra Tools > Device Manager e clique Create Device.
2. Escolha um perfil de telefone, como Pixel, e clique Next.
3. Escolha uma System Image compatível com a arquitetura do computador. Em PCs Intel ou AMD, normalmente será x86_64.
4. Se a imagem mostrar Download, baixe-a e aceite as licenças.
5. Clique Next, depois Finish.
6. No Device Manager, clique no botão de iniciar ao lado do dispositivo e espere a tela inicial do Android aparecer.

## 5. Execute o aplicativo

1. Na barra superior do Android Studio, selecione a configuração app.
2. Selecione o emulador que acabou de iniciar como dispositivo de destino.
3. Clique no botão verde Run.
4. Aguarde a instalação e a abertura do BebidaJá.
5. Na escolha de experiência, selecione **Explorar como cliente** ou **Explorar como empresa**. No cliente, use Início, Pesquisa, Pedidos e Perfil. Para trocar de perfil, abra Perfil e escolha **Sair da demonstração**. O comprovante e o pagamento são fictícios.

Para compilar: `./gradlew.bat :app:assembleDebug`. No Windows com caminho contendo acentos, execute `./test-unit.ps1` para os testes unitários; ele usa uma unidade temporária de caminho ASCII. Com emulador conectado (`adb devices`), execute `./gradlew.bat :app:connectedDebugAndroidTest`. A primeira execução pode exigir acesso à rede para baixar dependências.

Se a configuração app não aparecer, abra Run > Edit Configurations, adicione uma configuração Android App e selecione o módulo app.

## Problemas comuns

### SDK location not found

Abra Tools > SDK Manager e confira Android SDK Location. Abra a pasta raiz do projeto no Android Studio e sincronize novamente. O Android Studio configura local.properties para o computador; esse arquivo é ignorado pelo Git e não deve ser compartilhado.

### Erro de Java ou Gradle JDK

Em File > Settings > Build, Execution, Deployment > Build Tools > Gradle, escolha o Embedded JDK em Gradle JDK e sincronize novamente.

### Erro dizendo que o caminho contém caracteres não ASCII

O caminho atual contém acento em Área de Trabalho. O projeto já tem android.overridePathCheck=true em gradle.properties para permitir esse caminho. Depois de conferir essa linha, tente File > Sync Project with Gradle Files novamente.

### O emulador não inicia ou fica muito lento

Confirme que há pelo menos 5 GB livres em disco e atualize Android Emulator pelo SDK Manager. No Windows, a aceleração depende da virtualização do processador e de um hypervisor compatível, como Windows Hypervisor Platform. Consulte a documentação oficial de [aceleração do emulador](https://developer.android.com/studio/run/emulator-acceleration).

### API 37 mostra erro ao entrar na Play Store

Este aplicativo não precisa de login Google para a demonstração. Se estiver usando uma imagem API 37 com Play Store e ela tiver esse problema, atualize a imagem para a revisão 5 ou superior no SDK Manager, ou crie um emulador com API 36. Veja a página oficial de [problemas conhecidos do emulador](https://developer.android.com/studio/run/emulator-troubleshooting).

Se ainda não funcionar, envie uma captura da mensagem e o trecho vermelho da janela Build. A mensagem exata permite identificar em qual etapa o problema ocorre.
