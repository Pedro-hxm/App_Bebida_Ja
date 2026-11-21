# Visão geral da arquitetura

O aplicativo é nativo para Android e usa Kotlin, Jetpack Compose e Material 3.

- MainActivity inicia o tema e a navegação.
- ui/theme guarda as cores e o tema visual.
- ui/navigation define as rotas entre as telas.
- ui/screens contém a abertura, a tela inicial e as telas demonstrativas.
- ui/components reúne cartões, títulos e seletores reutilizados.
- data contém apenas os dados estáticos da demonstração.

As telas de busca, pedidos, perfil e empresa são pontos de partida visuais. Os dados não são compartilhados entre dispositivos e não existe servidor nesta base.