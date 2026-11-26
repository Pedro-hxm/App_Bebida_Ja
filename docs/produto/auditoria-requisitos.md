# Auditoria da versão integrada

Referência de trabalho: README, controle interno e código desta branch. Os três PDFs citados no planejamento não estavam no checkout durante esta auditoria; o plano entregue ao professor continua prevalecendo. Estado registrado em 26/11/2026, sujeito a nova validação após integração de outras branches.

Classificação: **validado** significa que a função descrita foi exercitada no escopo indicado, não que todas as combinações ou dispositivos foram testados. **Implementado, não validado** significa evidência no código sem teste completo nesta auditoria. **Parcial** indica lacuna ou validação insuficiente para o requisito completo.

| Requisito | Situação | Evidência e limite |
|---|---|---|
| RF01 Cliente: cadastro/login | Implementado, não validado | Telas e Firebase Authentication/Firestore existem; cadastro e login reais não foram exercitados nesta auditoria. |
| RF02 Empresa: cadastro/login | Implementado, não validado | Fluxo de perfil comercial existe; credenciais reais e serviços remotos não foram testados. |
| RF03 Perfil comercial | Parcial | Campos e imagem local implementados; não foi validado o ciclo completo de uma conta real com imagem. |
| RF04 Gestão de bebidas | Parcial | Cadastro, edição, remoção e disponibilidade implementados; pausa e retomada demonstrativas testadas. Ciclo completo de cadastro/edição/remoção não foi revalidado nesta auditoria. |
| RF05 Empresas por região | Implementado, não validado | Home e filtros no código; não se comprovou toda a matriz de regiões. |
| RF06 Catálogo | Parcial | Nome, volume, categoria, preço e disponibilidade observados; teste com imagem real e todos os estados permanece pendente. |
| RF07 Pesquisa e filtros | Implementado, não validado | Implementação e testes unitários existentes; não foi revalidada toda a matriz de pesquisa na interface. |
| RF08 Carrinho | Parcial | Remoção, estado vazio, volume e total observados; testes unitários cobrem quantidades e empresas distintas. Falta validar imagem real e mais cenários pela interface. |
| RF09 Endereço | Parcial | Endereço válido usado no emulador e inválido coberto no teste do pedido; seleção de endereços previamente cadastrados não foi demonstrada. |
| RF10 Checkout visual | Validado | Pix demonstrativo, resumo e total exercitados no emulador; não existe cobrança real. Outras formas não foram revalidadas. |
| RF11 Comprovante | Validado | Envio e referência fictícios visualizados; não há arquivo nem validação bancária. |
| RF12 Pedido e status | Parcial | Criação e Recebido → Em preparação observados entre perfis no mesmo aparelho; as quatro transições e reabertura do banco passaram em teste instrumentado. Todas as etapas ainda precisam ser exercitadas pela interface. |
| RF13 Histórico | Parcial | Listagem, detalhes e status atual observados no cliente; recuperação após reabrir o banco coberta por teste instrumentado. |
| RF14 Painel da empresa | Parcial | Pedido recebido, atualização e feedback observados; isolamento por empresa testado no banco. Ciclo completo de gestão e tratamento de falhas na interface ainda pendentes. |

## Áreas previstas

As 20 áreas do controle interno estão representadas em telas, abas ou seções do código: abertura; escolha de perfil; login; cadastros de cliente e empresa; início; pesquisa; empresa; catálogo; produto; carrinho; endereço; checkout; comprovante; acompanhamento; histórico; perfil; painel; gestão de produto; e pedidos recebidos. Representação no código não equivale à validação integral de cada área.

## Testes e limites

- `assembleDebug` e cinco testes instrumentados passaram no emulador Pixel 8 no ciclo da Etapa 4. Eles verificam persistência, migração, isolamento e transições.
- O comando direto `:app:testDebugUnitTest` falhou ao carregar classes neste caminho Windows com acento; `test-unit.ps1 --offline` passou usando o caminho ASCII temporário. A suíte inclui falha simulada de gravação, endereço inválido, empresas distintas e idempotência. A falha de gravação não foi injetada pela interface.
- O pedido demonstrativo criado na validação foi atualizado por uma empresa e visto pelo cliente no mesmo banco local. Não há sincronização entre aparelhos.
- O botão de produto indisponível foi observado desativado; não foi comprovada uma mudança de disponibilidade depois que um item já está no carrinho.
- Sem evidência suficiente de testes em outros tamanhos de tela, leitor de tela, contraste medido ou contas Firebase reais. A interface precisa de revisão adicional nesses contextos.
- Respostas de login/cadastro de telas já encerradas são ignoradas pela interface; a operação de autenticação em si não é cancelada. Esse caso precisa ser exercitado com Firebase real.
- O plano menciona controles e avisos para bebidas alcoólicas; o mecanismo precisa de definição e validação pela equipe. Não assumir que está concluído.
- A integração de imagens e volume deve ser acordada com os responsáveis pelos contratos do catálogo e do carrinho. Imagens armazenadas no aparelho não são compartilhadas entre dispositivos.
