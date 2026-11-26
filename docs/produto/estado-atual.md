# Estado atual do produto

## Funcionalidades integradas nesta etapa

- Login e cadastro continuam usando Firebase Authentication quando a configuração Firebase está disponível. O cadastro comercial da empresa coleta nome, endereço, telefone, horário e área de entrega.
- Empresas, produtos e pedidos são guardados no SQLite do aparelho. A base vem com estabelecimentos e produtos fictícios para a demonstração.
- A Home, a busca e os detalhes da empresa leem os dados persistidos. A empresa pode cadastrar, editar, pausar/retomar e remover produtos.
- O pedido guarda uma cópia dos itens, preço, volume, imagem, endereço, pagamento demonstrativo e status. O histórico do cliente oferece detalhes e acompanhamento; o painel da empresa consulta a mesma base e apresenta detalhes dos pedidos.
- A empresa atualiza os pedidos somente na sequência Recebido → Em preparação → Saiu para entrega → Entregue. A consulta é limitada ao identificador da empresa autenticada.
- O perfil mostra os dados comerciais persistidos da empresa ou os dados da conta disponíveis.

## Limites conhecidos

O SQLite é local ao aparelho e não sincroniza contas ou pedidos entre dispositivos. O cadastro de conta e o tipo de perfil usam Firebase quando configurado; os dados comerciais, produtos e pedidos desta branch são locais. Os dados iniciais são demonstrativos. Pagamentos e comprovantes não têm processamento financeiro real. Imagens locais de empresa e produto são suportadas; não há upload real de comprovantes.

Busca local aceita nome, categoria ou descrição. Não há serviço remoto, painel administrativo nem sincronização em nuvem nesta etapa.
