# Roteiro de demonstração — BebidaJá

## Abertura

O BebidaJá é um projeto acadêmico Android para conectar clientes a estabelecimentos de bebidas de Goiana e região. A demonstração utiliza dados fictícios no próprio aparelho.

## Tecnologia e equipe

- Kotlin, Jetpack Compose, Navigation Compose, Material 3 e SQLite local.
- Firebase Authentication e Firestore para contas e perfis quando configurados; catálogo e pedidos não são sincronizados pelo Firebase.
- Guilherme: cadastro, autenticação e dados; Arllan: estabelecimentos, catálogo, busca e produtos; Pedro Antonio: carrinho, checkout e histórico; Pedro Henrique: interface, navegação, acompanhamento, integração e qualidade. As entregas são compartilhadas e integradas pela equipe.

## Preparação

Abra o projeto no Android Studio conforme [como executar](../COMO_EXECUTAR.md), inicie um emulador e execute `app`. Não use dados bancários reais. Confirme antecipadamente que a Adega Central tem produtos disponíveis.

## Cliente

1. Na escolha inicial, selecione **Explorar como cliente**.
2. Apresente Home, busca, estabelecimento, catálogo e detalhes de um produto.
3. Adicione produtos da mesma empresa e mostre quantidade, remoção, volume e total no carrinho.
4. Informe um endereço fictício, escolha uma forma de pagamento e mostre o resumo.
5. Simule o comprovante e crie o pedido. No histórico, abra **Ver detalhes e acompanhamento** e mostre os quatro estados.

## Empresa

1. No perfil do cliente, escolha **Sair da demonstração** e selecione **Explorar como empresa** no mesmo aparelho.
2. Mostre os dados comerciais e o acesso ao gerenciamento do catálogo.
3. No painel, role até **Pedidos recebidos**, abra os detalhes e avance o pedido uma etapa.
4. Saia da demonstração da empresa, retorne como cliente e mostre o status atualizado no histórico.

## Encerramento e limites

Explique que SQLite guarda os dados neste aparelho: dois celulares não compartilham pedidos. O modo demonstrativo, o comprovante e o pagamento são simulações acadêmicas. Não há GPS em tempo real nem cobrança real. Contas reais exigem Firebase configurado e devem ser validadas separadamente. Consulte a [auditoria](../produto/auditoria-requisitos.md) para as pendências e evite apresentar como concluído o que não foi validado.
