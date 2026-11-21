# Configuração do Firebase

## Estado atual

O app já contém a integração com Firebase Authentication e Cloud Firestore. Ainda não há projeto vinculado nem arquivo `google-services.json`: a conta usada no Console atingiu o limite de criação de projetos, e nenhum projeto existente foi alterado. Enquanto isso, o app compila, mantém a demonstração disponível e desabilita os botões de login e cadastro.

## Ativar a integração

1. Escolha um projeto Firebase próprio para o Bebida Já ou libere a criação de um projeto novo.
2. No projeto, adicione um app Android com o ID de pacote exato `br.com.bebidaja`.
3. Baixe `google-services.json` e coloque-o na pasta `app/` do projeto Android.
4. Em Authentication, habilite o provedor **E-mail/senha**.
5. Crie o banco Cloud Firestore e escolha sua região antes de concluir a criação.
6. Copie as regras de [`firebase/regras_perfis_firestore.rules`](firebase/regras_perfis_firestore.rules) para a aba **Regras** do Firestore e publique-as.
7. Sincronize o Gradle e compile o app. A presença de `app/google-services.json` ativa o plugin Google Services; sem o arquivo, o build continua compilando para permitir o modo de demonstração.

Referências oficiais: [adicionar Firebase ao app Android](https://firebase.google.com/docs/android/setup), [autenticação por e-mail e senha](https://firebase.google.com/docs/auth/android/password-auth) e [regras de segurança do Firestore](https://firebase.google.com/docs/firestore/security/rules-conditions).

## Perfil armazenado

Cada conta usa `usuarios/{uid}` como documento, com os campos `uid`, `nome`, `email` e `tipoPerfil` (`cliente` ou `empresa`). O app grava o perfil após criar a conta e carrega o tipo de perfil após o login.

As regras permitem que a pessoa leia e mantenha somente o próprio documento. Listagem e exclusão ficam bloqueadas; e-mail, UID e tipo de perfil não podem ser alterados. O tipo é escolhido no cadastro e controla a navegação do protótipo; ele ainda não concede permissões administrativas para operações comerciais.

Esta etapa não conecta catálogo, estoque, pedidos ou pagamentos ao Firestore. O projeto continua podendo usar Room para persistência local em etapas futuras, mas os perfis on-line desta branch usam Firebase.
