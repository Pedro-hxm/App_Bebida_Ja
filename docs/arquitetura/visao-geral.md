# Visão geral da arquitetura

O aplicativo é nativo para Android e usa Kotlin, Jetpack Compose e Material 3.

- `AtividadePrincipal` inicializa o Firebase quando configurado e inicia a navegação.
- `ui/navigation` controla as sessões de cliente/empresa, as rotas e as permissões de cada perfil.
- `ui/screens` contém as jornadas de cadastro, Home, busca, detalhes, carrinho, checkout, perfil, painel comercial e pedidos.
- `data/PersistenciaLocal.kt` define os modelos comerciais e usa SQLite para empresas, produtos, pedidos e itens de pedido.
- `data/RepositorioPedidos.kt` declara o contrato de consulta e gravação dos pedidos.
- Firebase Authentication mantém a autenticação de contas. O documento `usuarios/{uid}` registra nome, e-mail e tipo de perfil.

O banco `bebidaja.db` persiste os dados no aparelho. Pedidos guardam snapshots dos produtos para preservar nome e preço mesmo quando o catálogo muda. A atualização de status verifica a empresa proprietária e aceita somente a próxima etapa do fluxo. A implementação é local e não sincroniza com outros dispositivos.
