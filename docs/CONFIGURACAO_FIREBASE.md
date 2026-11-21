# Configuração do Firebase

## Projeto vinculado

- Projeto Firebase: `bebidaja-59d5f`
- App Android: `br.com.bebidaja`
- Arquivo de configuração: `app/google-services.json`

O arquivo de configuração contém identificadores públicos do Firebase. As permissões de acesso aos dados dependem do Firebase Authentication e das regras do Firestore; não use essa chave para APIs Google que não sejam do Firebase.

## Ativação dos serviços

No Console do Firebase do projeto:

1. Em **Authentication > Método de login**, habilite **E-mail/senha**.
2. Crie o banco padrão do **Cloud Firestore**, edição Standard, em São Paulo (`southamerica-east1`). A localização não pode ser alterada depois da criação.
3. Depois que o banco existir, publique as regras de [`firebase/regras_perfis_firestore.rules`](firebase/regras_perfis_firestore.rules).
4. No Android Studio, sincronize o Gradle e compile o app.

O app encontra o arquivo em `app/google-services.json` para inicializar o Firebase. Até habilitar o provedor e criar o banco, login, cadastro e gravação de perfis não funcionarão.

## Perfil armazenado

Cada conta usa `usuarios/{uid}` como documento, com os campos `uid`, `nome`, `email` e `tipoPerfil` (`cliente` ou `empresa`). O app grava o perfil após criar a conta e carrega o tipo de perfil após o login.

As regras permitem que a pessoa leia e mantenha somente o próprio documento. Listagem e exclusão ficam bloqueadas; e-mail, UID e tipo de perfil não podem ser alterados. O tipo é escolhido no cadastro e controla a navegação do protótipo; ele ainda não concede permissões administrativas para operações comerciais.

Esta etapa não conecta catálogo, estoque, pedidos ou pagamentos ao Firestore. O projeto continua podendo usar Room para persistência local em etapas futuras; os perfis on-line usam Firebase.

Referências oficiais: [configuração do Firebase para Android](https://firebase.google.com/docs/android/setup), [autenticação por e-mail e senha](https://firebase.google.com/docs/auth/android/password-auth) e [regras de segurança do Firestore](https://firebase.google.com/docs/firestore/security/rules-conditions).
