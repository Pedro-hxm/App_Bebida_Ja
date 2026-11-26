# Sessões, carrinho e validação

## Isolamento do estado

O carrinho, o endereço de entrega, a forma de pagamento e a chave de confirmação são estado temporário em memória. A chave de sessão usada pelo Compose separa cada usuário autenticado, o perfil cliente de demonstração, o perfil empresa de demonstração e a sessão anônima.

Ao sair da conta ou trocar de perfil, o app esvazia o carrinho, remove endereço e forma de pagamento e gera uma nova chave de confirmação. Uma mudança de UID observada pelo Firebase também encerra a sessão local e retorna ao login, exigindo nova autenticação antes de mostrar dados da conta seguinte. Após um pedido confirmado, o carrinho é limpo e a chave de confirmação é renovada.

Os pedidos continuam persistidos no SQLite e são consultados pelo UID do cliente autenticado. Limpar o carrinho não apaga pedidos já registrados.

## Testes

No Windows, rode os testes unitários com:

```powershell
.\test-unit.ps1
```

O script usa temporariamente uma letra de unidade `SUBST` para expor a raiz por um caminho ASCII. Isso evita `ClassNotFoundException` no processo do Gradle quando o checkout está em um caminho com acentos, como `Área de Trabalho`.

Com um emulador ou dispositivo conectado, rode os testes instrumentados SQLite com:

```powershell
.\gradlew.bat --no-daemon connectedDebugAndroidTest
```

Os testes unitários cobrem limpeza da sessão, troca de UID e separação das chaves entre usuários e perfis de demonstração. Os testes instrumentados cobrem persistência SQLite, migração, isolamento dos dados por empresa e histórico de pedidos por cliente.

## Verificação manual de troca de conta

1. Entre com a conta cliente A, adicione itens ao carrinho e informe endereço e pagamento.
2. Encerre a sessão pelo perfil.
3. Entre com a conta cliente B e abra carrinho, endereço e checkout: nenhum estado da conta A deve permanecer.
4. Repita alternando cliente autenticado, demonstração cliente e demonstração empresa.
5. Confirme que o histórico de pedidos de cada cliente lista somente pedidos associados ao próprio UID.
